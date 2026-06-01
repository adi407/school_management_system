import { Component, signal, inject, OnInit } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { RouterModule } from '@angular/router';
import { DashboardService, ParentDashboard } from '../../../core/services/dashboard.service';

@Component({
  selector: 'sms-parent-dashboard',
  standalone: true,
  imports: [DatePipe, DecimalPipe, RouterModule],
  templateUrl: './parent-dashboard.component.html',
  styleUrls: ['./parent-dashboard.component.scss'],
})
export class ParentDashboardComponent implements OnInit {
  private dashSvc = inject(DashboardService);

  today   = new Date();
  loading = signal(true);
  error   = signal<string | null>(null);
  data    = signal<ParentDashboard | null>(null);

  ngOnInit() {
    this.dashSvc.getParentDashboard().subscribe({
      next: d => { this.data.set(d); this.loading.set(false); },
      error: () => { this.error.set('Could not load dashboard data.'); this.loading.set(false); },
    });
  }

  get child() {
    const d = this.data();
    if (!d) return null;
    const parts = d.studentName.split(' ');
    return {
      name: d.studentName,
      class: d.className ?? '—',
      rollNumber: d.rollNo ?? '—',
      admissionNumber: d.admissionNo,
      photoInitials: (parts[0]?.[0] ?? '') + (parts[1]?.[0] ?? ''),
    };
  }

  get attendance() {
    const a = this.data()?.attendance;
    return {
      percentage: a?.attendancePercent ?? 0,
      present:    a?.presentDays ?? 0,
      absent:     a?.absentDays ?? 0,
      late:       a?.lateDays ?? 0,
      total:      a?.totalDays ?? 0,
    };
  }

  get feeSummary() {
    const f = this.data()?.fees;
    if (!f) return { totalFee: 0, paid: 0, pending: 0 };
    return { totalFee: f.totalFees, paid: f.totalPaid, pending: f.balance };
  }

  get upcomingHomework() { return this.data()?.upcomingHomework ?? []; }
  get notices()          { return this.data()?.announcements ?? []; }

  noticeColor(roles: string[]): string {
    if (roles.length === 0) return 'blue';
    if (roles.includes('PARENT')) return 'green';
    return 'blue';
  }
}
