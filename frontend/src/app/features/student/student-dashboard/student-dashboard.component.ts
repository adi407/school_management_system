import { Component, signal, inject, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { DashboardService, StudentDashboard } from '../../../core/services/dashboard.service';

@Component({
  selector: 'sms-student-dashboard',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './student-dashboard.component.html',
  styleUrls: ['./student-dashboard.component.scss'],
})
export class StudentDashboardComponent implements OnInit {
  private dashSvc = inject(DashboardService);

  today    = new Date();
  loading  = signal(true);
  error    = signal<string | null>(null);
  data     = signal<StudentDashboard | null>(null);

  attendance    = signal({ percentage: 0, present: 0, total: 0 });
  todayClasses  = signal<{ time: string; subject: string; teacher: string; room: string }[]>([]);
  upcomingHomework = signal<{ dueDate: string; subject: string | null; title: string }[]>([]);

  ngOnInit() {
    this.dashSvc.getStudentDashboard().subscribe({
      next: d => {
        this.data.set(d);
        this.attendance.set({
          percentage: d.attendance?.attendancePercent ?? 0,
          present:    d.attendance?.presentDays ?? 0,
          total:      d.attendance?.totalDays ?? 0,
        });
        this.todayClasses.set(
          (d.todayTimetable ?? [])
            .sort((a, b) => a.startTime.localeCompare(b.startTime))
            .map(s => ({
              time:    s.startTime.slice(0, 5),
              subject: s.subjectName ?? '—',
              teacher: s.teacherName ?? '',
              room:    s.roomNo ?? '',
            }))
        );
        this.upcomingHomework.set(
          (d.upcomingHomework ?? []).map(h => ({
            dueDate: h.dueDate,
            subject: h.subjectName,
            title:   h.title,
          }))
        );
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Could not load dashboard data.');
        this.loading.set(false);
      },
    });
  }

  gradeColor(g: string) {
    return g.startsWith('A') ? 'green' : g.startsWith('B') ? 'blue' : 'yellow';
  }
}
