import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PtmService, PtmMeetingDto, PtmBriefingDto } from '../../../core/services/ptm.service';
import { AcademicService } from '../../../core/services/academic.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'sms-ptm-prep',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ptm-prep.component.html',
  styleUrls: ['./ptm-prep.component.scss'],
})
export class PtmPrepComponent implements OnInit {
  private ptmSvc     = inject(PtmService);
  private academicSvc = inject(AcademicService);
  private toast      = inject(ToastService);

  // State
  meetings   = signal<PtmMeetingDto[]>([]);
  briefings  = signal<PtmBriefingDto[]>([]);
  classes    = signal<any[]>([]);
  years      = signal<any[]>([]);
  loading    = signal(false);
  generating = signal(false);

  // Selected meeting for detail view
  selectedMeeting = signal<PtmMeetingDto | null>(null);
  expandedBriefing = signal<string | null>(null);

  // Create meeting form
  showCreateForm = signal(false);
  newTitle = '';
  newClassId = '';
  newYearId = '';
  newDate = '';
  newNotes = '';

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    this.loading.set(true);
    this.ptmSvc.listMeetings().subscribe({
      next: data => { this.meetings.set(data); this.loading.set(false); },
      error: () => { this.loading.set(false); this.toast.error('Failed to load PTM meetings'); }
    });
    this.academicSvc.listClasses().subscribe({ next: data => this.classes.set(data) });
    this.academicSvc.listYears().subscribe({ next: data => this.years.set(data) });
  }

  createMeeting() {
    if (!this.newTitle || !this.newYearId || !this.newDate) return;
    this.ptmSvc.createMeeting({
      title: this.newTitle,
      classId: this.newClassId || undefined,
      academicYearId: this.newYearId,
      meetingDate: this.newDate,
      notes: this.newNotes || undefined
    }).subscribe({
      next: () => {
        this.toast.success('PTM meeting scheduled!');
        this.showCreateForm.set(false);
        this.newTitle = ''; this.newClassId = ''; this.newDate = ''; this.newNotes = '';
        this.loadData();
      },
      error: (err) => this.toast.error(err?.error?.message ?? 'Failed to create meeting')
    });
  }

  selectMeeting(m: PtmMeetingDto) {
    this.selectedMeeting.set(m);
    this.briefings.set([]);
    this.ptmSvc.getBriefings(m.id).subscribe({
      next: data => this.briefings.set(data)
    });
  }

  backToList() {
    this.selectedMeeting.set(null);
    this.briefings.set([]);
    this.expandedBriefing.set(null);
  }

  generateBriefings() {
    const m = this.selectedMeeting();
    if (!m) return;
    this.generating.set(true);
    this.ptmSvc.generateBriefings(m.id).subscribe({
      next: data => {
        this.briefings.set(data);
        this.generating.set(false);
        this.toast.success(`AI generated ${data.length} student briefings`);
      },
      error: (err) => {
        this.generating.set(false);
        this.toast.error(err?.error?.message ?? 'Failed to generate briefings');
      }
    });
  }

  toggleBriefing(id: string) {
    this.expandedBriefing.set(this.expandedBriefing() === id ? null : id);
  }

  reviewBriefing(briefingId: string) {
    this.ptmSvc.reviewBriefing(briefingId).subscribe({
      next: updated => {
        this.briefings.update(list => list.map(b => b.id === updated.id ? updated : b));
        this.toast.success('Briefing marked as reviewed');
      },
      error: () => this.toast.error('Failed to review briefing')
    });
  }

  statusIcon(status: string): string {
    switch (status) { case 'SCHEDULED': return '📅'; case 'IN_PROGRESS': return '🔵'; case 'COMPLETED': return '✅'; default: return '❌'; }
  }

  wellnessIcon(trend: string | null): string {
    switch (trend) { case 'IMPROVING': return '📈'; case 'DECLINING': return '📉'; default: return '➡️'; }
  }

  attendanceClass(pct: number | null): string {
    if (pct === null) return '';
    if (pct >= 90) return 'good';
    if (pct >= 75) return 'okay';
    return 'low';
  }
}
