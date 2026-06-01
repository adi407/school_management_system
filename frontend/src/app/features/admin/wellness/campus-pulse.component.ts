import { Component, signal, inject, OnInit, computed } from '@angular/core';
import { DatePipe, NgClass, DecimalPipe, KeyValuePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { WellnessService } from '../../../core/services/wellness.service';
import { AcademicService } from '../../../core/services/academic.service';
import { ToastService } from '../../../core/services/toast.service';
import { ClassPulseDto } from '../../../core/models/wellness.model';
import { ClassDto } from '../../../core/models/academic.model';
import { MOOD_CONFIG } from '../../../core/models/wellness.model';

/**
 * Campus Pulse — Admin / Counselor view.
 * Shows today's mood summary for all classes in the school.
 * Classes with ≥30% negative moods show a red alert banner.
 */
@Component({
  selector: 'sms-campus-pulse',
  standalone: true,
  imports: [DatePipe, NgClass, DecimalPipe, KeyValuePipe, FormsModule],
  templateUrl: './campus-pulse.component.html',
  styleUrls: ['./campus-pulse.component.scss'],
})
export class CampusPulseComponent implements OnInit {
  private wellnessService = inject(WellnessService);
  private toast           = inject(ToastService);

  readonly MOOD_CONFIG = MOOD_CONFIG;

  pulses    = signal<ClassPulseDto[]>([]);
  loading   = signal(true);
  error     = signal('');

  today = new Date().toISOString().split('T')[0];

  // Computed school-wide summary
  totalCheckins = computed(() => this.pulses().reduce((s, p) => s + p.totalCheckins, 0));
  alertClasses  = computed(() => this.pulses().filter(p => p.alertTriggered));
  overallPositivePct = computed(() => {
    const total    = this.totalCheckins();
    if (!total) return 0;
    const positive = this.pulses().reduce((s, p) => s + p.positiveCount, 0);
    return Math.round((positive / total) * 100);
  });

  ngOnInit() {
    this.load();
  }

  load() {
    this.loading.set(true);
    this.wellnessService.getSchoolPulse().subscribe({
      next: data => {
        // Sort: alert classes first, then by check-in count desc
        const sorted = [...data].sort((a, b) => {
          if (a.alertTriggered !== b.alertTriggered) return a.alertTriggered ? -1 : 1;
          return b.totalCheckins - a.totalCheckins;
        });
        this.pulses.set(sorted);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Could not load Campus Pulse data.');
        this.loading.set(false);
      },
    });
  }

  /** Returns CSS width % for the mood bar */
  moodBarWidth(count: number, total: number): number {
    return total > 0 ? Math.round((count / total) * 100) : 0;
  }

  moodKeys = ['GREAT', 'GOOD', 'OKAY', 'SAD', 'STRESSED'] as const;
  moodCount(pulse: ClassPulseDto, mood: string): number {
    return pulse.moodBreakdown[mood] ?? 0;
  }
}
