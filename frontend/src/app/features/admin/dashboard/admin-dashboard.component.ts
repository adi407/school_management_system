import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { DatePipe, NgClass, DecimalPipe } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { DashboardService, AdminDashboard } from '../../../core/services/dashboard.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'sms-admin-dashboard',
  standalone: true,
  imports: [RouterModule, DatePipe, NgClass, DecimalPipe],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.scss'],
})
export class AdminDashboardComponent implements OnInit {
  private dashboardSvc = inject(DashboardService);
  private toast = inject(ToastService);
  auth = inject(AuthService);
  user = this.auth.user;
  today = new Date();

  loading = signal(true);
  data = signal<AdminDashboard | null>(null);

  greeting = computed(() => {
    const h = new Date().getHours();
    if (h < 12) return 'Good morning';
    if (h < 17) return 'Good afternoon';
    return 'Good evening';
  });

  // Computed accessors so template stays clean
  stats = computed(() => this.data() ?? {
    totalStudents: 0, activeStudents: 0, attendanceToday: 0,
    feeCollectedToday: 0, totalFeePending: 0, staffCount: 0,
    upcomingExams: 0, booksOverdue: 0, recentActivity: [],
  });

  modules = [
    { label: 'Students',     route: '/admin/students',    icon: '👥' },
    { label: 'Classes',      route: '/admin/classes',     icon: '🏫' },
    { label: 'Staff',        route: '/admin/staff',       icon: '👨‍🏫' },
    { label: 'Attendance',   route: '/admin/attendance',  icon: '✓'  },
    { label: 'Homework',     route: '/admin/homework',    icon: '📝' },
    { label: 'Campus Pulse', route: '/admin/pulse',       icon: '💙' },
    { label: 'Timetable',    route: '/admin/timetable',   icon: '📅' },
    { label: 'Exams',        route: '/admin/exams',       icon: '📋' },
    { label: 'Fees',         route: '/admin/fees',        icon: '💰' },
    { label: 'Library',      route: '/admin/library',     icon: '📚' },
    { label: 'Activities',   route: '/admin/activities',  icon: '🏆' },
  ];

  quickActions = [
    { label: 'Mark Attendance', icon: '✓',  route: '/admin/attendance',  color: 'green'  },
    { label: 'New Homework',    icon: '📝', route: '/admin/homework',    color: 'purple' },
    { label: 'Campus Pulse',    icon: '💙', route: '/admin/pulse',       color: 'blue'   },
    { label: 'Add Student',     icon: '+',  route: '/admin/students',    color: 'orange' },
  ];

  ngOnInit(): void {
    this.dashboardSvc.getAdminDashboard().subscribe({
      next: d => { this.data.set(d); this.loading.set(false); },
      error: () => { this.loading.set(false); this.toast.error('Failed to load dashboard data'); },
    });
  }

  activityIcon(type: string): string {
    const map: Record<string, string> = { fee: '💰', student: '👤', attendance: '✓', exam: '📝', grievance: '🎫' };
    return map[type] ?? '•';
  }
  activityColor(type: string): string {
    const map: Record<string, string> = {
      fee: 'var(--success)', student: 'var(--accent)',
      attendance: 'var(--accent-5)', exam: 'var(--warning)', grievance: 'var(--error)',
    };
    return map[type] ?? 'var(--text-muted)';
  }
}
