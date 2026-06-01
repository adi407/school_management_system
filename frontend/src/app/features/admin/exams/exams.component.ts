import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { DatePipe, NgClass } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ExamService, ExamDto, CreateExamRequest } from '../../../core/services/exam.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'sms-exams',
  standalone: true,
  imports: [RouterModule, DatePipe, NgClass, FormsModule],
  templateUrl: './exams.component.html',
  styleUrls: ['./exams.component.scss'],
})
export class ExamsComponent implements OnInit {
  private examSvc = inject(ExamService);
  private toast = inject(ToastService);

  loading = signal(true);
  saving  = signal(false);
  exams   = signal<ExamDto[]>([]);
  showModal = signal(false);
  editingId = signal<string | null>(null);

  form = signal<CreateExamRequest>({
    name: '', examType: 'MIDTERM', startDate: '', endDate: '',
    totalSubjects: 1, description: '', status: 'UPCOMING',
  });

  examTypes = ['MIDTERM', 'UNIT_TEST', 'ANNUAL', 'PREBOARD', 'QUARTERLY', 'HALF_YEARLY'];

  filterStatus = signal<string>('ALL');
  filteredExams = computed(() => {
    const s = this.filterStatus();
    return s === 'ALL' ? this.exams() : this.exams().filter(e => e.status === s);
  });

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.examSvc.list().subscribe({
      next: d => { this.exams.set(d); this.loading.set(false); },
      error: () => { this.loading.set(false); this.toast.error('Failed to load exams'); },
    });
  }

  openCreate(): void {
    this.editingId.set(null);
    this.form.set({ name: '', examType: 'MIDTERM', startDate: '', endDate: '', totalSubjects: 1, description: '', status: 'UPCOMING' });
    this.showModal.set(true);
  }

  openEdit(exam: ExamDto): void {
    this.editingId.set(exam.id);
    this.form.set({
      name: exam.name, examType: exam.examType,
      startDate: exam.startDate, endDate: exam.endDate,
      totalSubjects: exam.totalSubjects, description: exam.description ?? '',
      status: exam.status,
    });
    this.showModal.set(true);
  }

  save(): void {
    const f = this.form();
    if (!f.name || !f.startDate || !f.endDate) { this.toast.warning('Please fill in all required fields'); return; }
    this.saving.set(true);
    const id = this.editingId();
    const obs = id ? this.examSvc.update(id, f) : this.examSvc.create(f);
    obs.subscribe({
      next: () => {
        this.toast.success(id ? 'Exam updated' : 'Exam created');
        this.showModal.set(false);
        this.saving.set(false);
        this.load();
      },
      error: () => { this.toast.error('Failed to save exam'); this.saving.set(false); },
    });
  }

  delete(id: string): void {
    if (!confirm('Delete this exam?')) return;
    this.examSvc.delete(id).subscribe({
      next: () => { this.toast.success('Exam deleted'); this.load(); },
      error: () => this.toast.error('Failed to delete exam'),
    });
  }

  updateForm(patch: Partial<CreateExamRequest>): void {
    this.form.update(f => ({ ...f, ...patch }));
  }

  statusColor(s: string) {
    return s === 'UPCOMING' ? 'blue' : s === 'ONGOING' ? 'green' : s === 'COMPLETED' ? 'gray' : 'yellow';
  }
  typeLabel(t: string) {
    const m: Record<string, string> = {
      MIDTERM: 'Mid-Term', UNIT_TEST: 'Unit Test', ANNUAL: 'Annual',
      PREBOARD: 'Pre-Board', QUARTERLY: 'Quarterly', HALF_YEARLY: 'Half-Yearly',
    };
    return m[t] ?? t;
  }
}
