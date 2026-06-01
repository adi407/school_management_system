import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NgClass } from '@angular/common';
import { DashboardService, TeacherDashboard } from '../../../core/services/dashboard.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'sms-teacher-dashboard',
  standalone: true,
  imports: [RouterModule, DatePipe, NgClass],
  templateUrl: './teacher-dashboard.component.html',
  styleUrls: ['./teacher-dashboard.component.scss'],
})
export class TeacherDashboardComponent implements OnInit {
  private dashboardSvc = inject(DashboardService);
  private toast = inject(ToastService);

  today = new Date();
  loading = signal(true);
  data = signal<TeacherDashboard | null>(null);

  greeting = computed(() => {
    const h = new Date().getHours();
    if (h < 12) return 'Good morning';
    if (h < 17) return 'Good afternoon';
    return 'Good evening';
  });

  teacherName = computed(() => this.data()?.teacherName ?? '');
  todaySchedule = computed(() => this.data()?.todaySchedule ?? []);
  pendingHomework = computed(() => this.data()?.pendingHomework ?? []);
  totalHomework = computed(() => this.data()?.totalHomeworkAssigned ?? 0);
  classesCount = computed(() => this.data()?.classesTeachingToday ?? 0);

  ngOnInit(): void {
    this.dashboardSvc.getTeacherDashboard().subscribe({
      next: d => { this.data.set(d); this.loading.set(false); },
      error: () => { this.loading.set(false); this.toast.error('Failed to load dashboard data'); },
    });
  }

  dueColor(days: number): string {
    if (days <= 0) return 'red';
    if (days <= 2) return 'yellow';
    return 'green';
  }

  dueLabel(days: number): string {
    if (days < 0) return `${Math.abs(days)}d overdue`;
    if (days === 0) return 'Due today';
    if (days === 1) return 'Due tomorrow';
    return `Due in ${days}d`;
  }
}
