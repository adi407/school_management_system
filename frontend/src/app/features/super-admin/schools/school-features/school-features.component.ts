import { Component, inject, signal, OnInit } from '@angular/core';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { SchoolService } from '../../../../core/services/school.service';
import { ToastService } from '../../../../core/services/toast.service';
import { FeatureFlagsMap } from '../../../../core/models/feature-flag.model';
import { FEATURE_GROUPS } from '../../../../core/models/feature-flag.model';
import { FeatureKey } from '../../../../core/models/user.model';
import { SchoolDto, SubscriptionTier } from '../../../../core/models/school.model';

const CORE_KEYS: FeatureKey[] = ['STUDENT_MANAGEMENT', 'ATTENDANCE', 'ACADEMIC_MANAGEMENT', 'NOTICE_BOARD'];

@Component({
  selector: 'sms-school-features',
  standalone: true,
  imports: [RouterModule, FormsModule],
  templateUrl: './school-features.component.html',
  styleUrls: ['./school-features.component.scss'],
})
export class SchoolFeaturesComponent implements OnInit {
  private route     = inject(ActivatedRoute);
  private schoolSvc = inject(SchoolService);
  private toast     = inject(ToastService);

  schoolId = this.route.snapshot.paramMap.get('id')!;
  school   = signal<SchoolDto | null>(null);
  flags    = signal<FeatureFlagsMap>({});
  loading  = signal(true);
  saving   = signal(false);

  // local draft of toggle states
  draft         = signal<Record<string, boolean>>({});
  appliedPreset = signal<string | null>(null);

  featureGroups = FEATURE_GROUPS;
  coreKeys = CORE_KEYS;

  ngOnInit() {
    this.schoolSvc.getSchool(this.schoolId).subscribe(s => this.school.set(s));
    this.schoolSvc.getFeatures(this.schoolId).subscribe({
      next: (flags) => {
        this.flags.set(flags);
        const d: Record<string, boolean> = {};
        Object.entries(flags).forEach(([k, v]) => (d[k] = v.isEnabled));
        this.draft.set(d);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  toggle(key: string) {
    if (this.coreKeys.includes(key as FeatureKey)) return; // core always on
    this.draft.update(d => ({ ...d, [key]: !d[key] }));
  }

  applyTierPreset(tier: SubscriptionTier) {
    const presets: Record<SubscriptionTier, FeatureKey[]> = {
      FREE:       ['STUDENT_MANAGEMENT','ATTENDANCE','ACADEMIC_MANAGEMENT','NOTICE_BOARD'],
      BASIC:      ['STUDENT_MANAGEMENT','ATTENDANCE','ACADEMIC_MANAGEMENT','NOTICE_BOARD','EXAM_MANAGEMENT','FEE_MANAGEMENT','TIMETABLE','HOMEWORK','REPORT_CARDS'],
      PREMIUM:    ['STUDENT_MANAGEMENT','ATTENDANCE','ACADEMIC_MANAGEMENT','NOTICE_BOARD','EXAM_MANAGEMENT','FEE_MANAGEMENT','ONLINE_FEE_PAYMENT','STAFF_HR','PAYROLL','PARENT_PORTAL','TIMETABLE','HOMEWORK','DOCUMENT_MANAGEMENT','REPORT_CARDS','LIBRARY','TRANSPORT','GPS_TRACKING','HOSTEL','EXTRA_CURRICULAR','ACHIEVEMENTS','ONLINE_CLASSES','ASSIGNMENT_PORTAL'],
      ENTERPRISE: Object.keys(this.flags()) as FeatureKey[],
    };
    const enabled = new Set(presets[tier]);
    const d: Record<string, boolean> = {};
    Object.keys(this.flags()).forEach(k => (d[k] = enabled.has(k as FeatureKey)));
    this.draft.set(d);
    this.appliedPreset.set(tier);
  }

  save() {
    this.saving.set(true);
    this.schoolSvc.updateFeatures(this.schoolId, this.draft()).subscribe({
      next: () => {
        this.saving.set(false);
        this.toast.success('Feature flags saved successfully');
      },
      error: () => {
        this.saving.set(false);
        this.toast.error('Failed to save feature flags');
      },
    });
  }

  isCore(key: string): boolean { return this.coreKeys.includes(key as FeatureKey); }

  enabledCount(): number { return Object.values(this.draft()).filter(Boolean).length; }

  enabledInGroup(keys: FeatureKey[]): number {
    const d = this.draft();
    return keys.filter(k => d[k]).length;
  }

  keyLabel(key: string): string { return key.split('_').join(' '); }
}
