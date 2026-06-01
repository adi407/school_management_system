import { Component, inject, signal, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { DashboardService, HomeworkItem } from '../../../core/services/dashboard.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'sms-student-homework',
  standalone: true,
  imports: [DatePipe],
  template: `
    <div class="page-header">
      <div>
        <span class="page-header__tag">Student Portal</span>
        <h1 class="page-header__title">My Homework</h1>
        <p class="page-header__subtitle">Upcoming assignments and tasks</p>
      </div>
    </div>

    @if (loading()) {
      <div class="text-muted text-sm" style="padding:40px 0;text-align:center">Loading homework…</div>
    } @else if (homework().length === 0) {
      <div class="card" style="padding:60px;text-align:center">
        <div style="font-size:48px;margin-bottom:12px">✅</div>
        <div class="fw-600 mb-8">All caught up!</div>
        <div class="text-muted text-sm">No upcoming homework at the moment.</div>
      </div>
    } @else {
      <div class="hw-list">
        @for (hw of homework(); track hw.id) {
          <div class="hw-card card">
            <div class="hw-card__left">
              <span class="badge badge--{{ dueColor(hw.daysUntilDue) }}">{{ dueLabel(hw.daysUntilDue) }}</span>
            </div>
            <div class="hw-card__body">
              <div class="hw-card__title">{{ hw.title }}</div>
              <div class="hw-card__meta text-sm text-muted">
                {{ hw.subjectName ?? 'General' }}
                @if (hw.estimatedMinutes) { · ~{{ hw.estimatedMinutes }} min }
              </div>
              @if (hw.description) {
                <div class="hw-card__desc text-sm" style="margin-top:6px">{{ hw.description }}</div>
              }
            </div>
            <div class="hw-card__due text-xs text-muted">
              Due: {{ hw.dueDate | date:'EEE, d MMM' }}
            </div>
          </div>
        }
      </div>
    }
  `,
  styles: [`
    .hw-list { display: flex; flex-direction: column; gap: 12px; }
    .hw-card { display: flex; align-items: flex-start; gap: 16px; padding: 16px 20px; }
    .hw-card__body  { flex: 1; }
    .hw-card__title { font-size: 15px; font-weight: 600; color: var(--text-primary); margin-bottom: 4px; }
    .hw-card__desc  { color: var(--text-secondary); }
    .hw-card__due   { white-space: nowrap; padding-top: 2px; }
  `],
})
export class StudentHomeworkComponent implements OnInit {
  private dashSvc = inject(DashboardService);
  private toast   = inject(ToastService);

  loading  = signal(true);
  homework = signal<HomeworkItem[]>([]);

  ngOnInit(): void {
    this.dashSvc.getStudentDashboard().subscribe({
      next: d => { this.homework.set(d.upcomingHomework ?? []); this.loading.set(false); },
      error: () => { this.loading.set(false); this.toast.error('Failed to load homework'); },
    });
  }

  dueColor(days: number): string {
    if (days <= 0) return 'red';
    if (days <= 2) return 'yellow';
    return 'green';
  }

  dueLabel(days: number): string {
    if (days < 0)  return `${Math.abs(days)}d overdue`;
    if (days === 0) return 'Due today';
    if (days === 1) return 'Tomorrow';
    return `${days} days`;
  }
}
