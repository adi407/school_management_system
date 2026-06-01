import { Component, signal, inject, OnInit } from '@angular/core';
import { DatePipe, NgClass } from '@angular/common';
import { HomeworkService } from '../../../core/services/homework.service';
import { AuthService } from '../../../core/services/auth.service';
import { HomeworkDto } from '../../../core/models/homework.model';

/**
 * Parent Homework View — USP Feature.
 * Shows all upcoming + recent homework for the child's class.
 * Parents see assignments organised by subject, colour-coded by urgency.
 */
@Component({
  selector: 'sms-parent-homework',
  standalone: true,
  imports: [DatePipe, NgClass],
  templateUrl: './parent-homework.component.html',
  styleUrls: ['./parent-homework.component.scss'],
})
export class ParentHomeworkComponent implements OnInit {
  private homeworkService = inject(HomeworkService);
  private auth            = inject(AuthService);

  homework    = signal<HomeworkDto[]>([]);
  loading     = signal(true);
  error       = signal('');

  // In a real implementation the parent's linked class_id comes from their student profile.
  // For now we use a mock classId — wire this up when parent-student linking is built.
  classId = '';  // populated in ngOnInit from auth or child profile

  // Date range: today to 30 days ahead
  private from = new Date().toISOString().split('T')[0];
  private to   = (() => { const d = new Date(); d.setDate(d.getDate() + 30); return d.toISOString().split('T')[0]; })();

  activeTab = signal<'upcoming' | 'all'>('upcoming');

  upcomingCount = signal(0);
  overdueCount  = signal(0);

  ngOnInit() {
    // TODO: resolve classId from auth.user()'s linked student profile
    // For demo: load upcoming for a hardcoded or resolved class
    // When real parent-student relationship is implemented, fetch classId from the student profile
    this.loadUpcoming();
  }

  loadUpcoming() {
    if (!this.classId) {
      // No classId yet — show empty state without error
      this.loading.set(false);
      return;
    }
    this.loading.set(true);
    this.homeworkService.getUpcoming(this.classId).subscribe({
      next: hw => {
        this.homework.set(hw);
        this.upcomingCount.set(hw.filter(h => h.daysUntilDue >= 0).length);
        this.overdueCount.set(hw.filter(h => h.daysUntilDue < 0).length);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Could not load homework. Please try again.');
        this.loading.set(false);
      },
    });
  }

  urgencyClass(days: number): string {
    if (days < 0)   return 'urgent--overdue';
    if (days === 0) return 'urgent--today';
    if (days <= 2)  return 'urgent--soon';
    return 'urgent--normal';
  }

  urgencyLabel(days: number): string {
    if (days < 0)   return `⚠ Overdue by ${Math.abs(days)} day${Math.abs(days) > 1 ? 's' : ''}`;
    if (days === 0) return '🔔 Due today!';
    if (days === 1) return '⏰ Due tomorrow';
    return `📅 Due in ${days} days`;
  }
}
