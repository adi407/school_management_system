import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SubstituteService, SubstituteAssignmentDto, SubstituteSuggestionDto } from '../../../core/services/substitute.service';
import { StaffService } from '../../../core/services/staff.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'sms-smart-substitute',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './smart-substitute.component.html',
  styleUrls: ['./smart-substitute.component.scss'],
})
export class SmartSubstituteComponent implements OnInit {
  private subSvc  = inject(SubstituteService);
  private staffSvc = inject(StaffService);
  private toast   = inject(ToastService);

  // State
  selectedDate = signal(new Date().toISOString().split('T')[0]);
  assignments  = signal<SubstituteAssignmentDto[]>([]);
  teachers     = signal<any[]>([]);
  loading      = signal(false);
  reporting    = signal(false);

  // Report absence form
  selectedTeacherId = '';
  absenceRemarks    = '';

  // Suggestions modal
  showSuggestions = signal(false);
  activeAssignment = signal<SubstituteAssignmentDto | null>(null);
  suggestions      = signal<SubstituteSuggestionDto[]>([]);
  loadingSuggestions = signal(false);

  ngOnInit() {
    this.loadTeachers();
    this.loadAssignments();
  }

  loadTeachers() {
    this.staffSvc.list().subscribe({
      next: staff => this.teachers.set(staff.filter((s: any) => s.role === 'TEACHER' && s.isActive)),
      error: () => this.toast.error('Failed to load teachers')
    });
  }

  loadAssignments() {
    this.loading.set(true);
    this.subSvc.getByDate(this.selectedDate()).subscribe({
      next: data => { this.assignments.set(data); this.loading.set(false); },
      error: () => { this.loading.set(false); this.toast.error('Failed to load assignments'); }
    });
  }

  onDateChange(date: string) {
    this.selectedDate.set(date);
    this.loadAssignments();
  }

  reportAbsence() {
    if (!this.selectedTeacherId) return;
    this.reporting.set(true);
    this.subSvc.reportAbsence(this.selectedTeacherId, this.selectedDate(), this.absenceRemarks).subscribe({
      next: data => {
        this.reporting.set(false);
        if (data.length === 0) {
          this.toast.info('Teacher has no classes on this day');
        } else {
          this.toast.success(`${data.length} periods need coverage — AI suggestions generated`);
          this.loadAssignments();
        }
        this.selectedTeacherId = '';
        this.absenceRemarks = '';
      },
      error: (err) => {
        this.reporting.set(false);
        this.toast.error(err?.error?.message ?? 'Failed to report absence');
      }
    });
  }

  openSuggestions(assignment: SubstituteAssignmentDto) {
    this.activeAssignment.set(assignment);
    this.showSuggestions.set(true);
    this.loadingSuggestions.set(true);
    this.subSvc.getSuggestions(assignment.id).subscribe({
      next: data => { this.suggestions.set(data); this.loadingSuggestions.set(false); },
      error: () => { this.loadingSuggestions.set(false); this.toast.error('Failed to load suggestions'); }
    });
  }

  closeSuggestions() {
    this.showSuggestions.set(false);
    this.activeAssignment.set(null);
    this.suggestions.set([]);
  }

  assignTeacher(substituteTeacherId: string | null) {
    const a = this.activeAssignment();
    if (!a) return;
    this.subSvc.assignSubstitute(a.id, substituteTeacherId).subscribe({
      next: () => {
        this.toast.success(substituteTeacherId ? 'Substitute assigned!' : 'Marked as self-study');
        this.closeSuggestions();
        this.loadAssignments();
      },
      error: () => this.toast.error('Failed to assign substitute')
    });
  }

  statusColor(status: string): string {
    switch (status) {
      case 'ASSIGNED':  return 'green';
      case 'SUGGESTED': return 'blue';
      case 'SELF_STUDY': return 'yellow';
      case 'CANCELLED': return 'red';
      default:          return 'gray';
    }
  }

  confidenceLabel(score: number | null): string {
    if (score === null) return '';
    if (score >= 0.8) return 'High match';
    if (score >= 0.5) return 'Good match';
    return 'Low match';
  }

  confidenceColor(score: number): string {
    if (score >= 0.8) return 'green';
    if (score >= 0.5) return 'blue';
    return 'yellow';
  }

  get pendingCount(): number {
    return this.assignments().filter(a => a.status === 'PENDING' || a.status === 'SUGGESTED').length;
  }

  get assignedCount(): number {
    return this.assignments().filter(a => a.status === 'ASSIGNED').length;
  }
}
