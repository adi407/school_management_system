import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { DashboardService, TimetableSlot } from '../../../core/services/dashboard.service';
import { ToastService } from '../../../core/services/toast.service';

const DAYS = ['MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT'];
const DAY_LABELS: Record<string, string> = {
  MON: 'Monday', TUE: 'Tuesday', WED: 'Wednesday',
  THU: 'Thursday', FRI: 'Friday', SAT: 'Saturday',
};

@Component({
  selector: 'sms-student-timetable',
  standalone: true,
  imports: [],
  template: `
    <div class="page-header">
      <div>
        <span class="page-header__tag">Student Portal</span>
        <h1 class="page-header__title">My Timetable</h1>
        <p class="page-header__subtitle">Weekly class schedule</p>
      </div>
    </div>

    @if (loading()) {
      <div class="text-muted text-sm" style="padding:40px 0;text-align:center">Loading timetable…</div>
    } @else {
      <div class="timetable-days">
        @for (day of DAYS; track day) {
          @if (slotsByDay()[day] && slotsByDay()[day].length) {
            <div class="day-section">
              <h2 class="day-label">{{ DAY_LABELS[day] }}</h2>
              <div class="day-slots card" style="overflow:hidden">
                @for (slot of slotsByDay()[day]; track slot.id) {
                  <div class="slot-row" [class.slot-row--today]="isToday(day)">
                    <div class="slot-time font-mono text-xs text-muted">{{ slot.startTime.slice(0,5) }} – {{ slot.endTime.slice(0,5) }}</div>
                    <div class="slot-body">
                      <div class="fw-600 text-sm">{{ slot.subjectName ?? 'Free Period' }}</div>
                      <div class="text-xs text-muted">
                        {{ slot.teacherName ?? '' }}{{ slot.roomNo ? ' · ' + slot.roomNo : '' }}
                      </div>
                    </div>
                    <div class="slot-period text-xs text-muted">P{{ slot.periodNo }}</div>
                  </div>
                }
              </div>
            </div>
          }
        }
        @if (allSlots().length === 0) {
          <div class="card" style="padding:60px;text-align:center">
            <div style="font-size:48px;margin-bottom:12px">📅</div>
            <div class="fw-600 mb-8">No timetable found</div>
            <div class="text-muted text-sm">Your timetable hasn't been set up yet.</div>
          </div>
        }
      </div>
    }
  `,
  styles: [`
    .timetable-days { display: flex; flex-direction: column; gap: 20px; }
    .day-label { font-size: 15px; font-weight: 700; color: var(--text-primary); margin-bottom: 10px; }
    .slot-row { display: flex; align-items: center; gap: 16px; padding: 14px 18px; border-bottom: 1px solid var(--border); }
    .slot-row:last-child { border-bottom: none; }
    .slot-row--today { background: rgba(var(--accent-rgb),.04); }
    .slot-time { width: 100px; flex-shrink: 0; }
    .slot-body { flex: 1; }
    .slot-period { width: 30px; text-align: right; }
  `],
})
export class StudentTimetableComponent implements OnInit {
  private dashSvc = inject(DashboardService);
  private toast   = inject(ToastService);

  DAYS = DAYS;
  DAY_LABELS = DAY_LABELS;

  loading  = signal(true);
  allSlots = signal<TimetableSlot[]>([]);

  slotsByDay = computed(() => {
    const map: Record<string, TimetableSlot[]> = {};
    for (const slot of this.allSlots()) {
      if (!map[slot.dayOfWeek]) map[slot.dayOfWeek] = [];
      map[slot.dayOfWeek].push(slot);
    }
    // Sort each day by start time
    for (const day of Object.keys(map)) {
      map[day].sort((a, b) => a.startTime.localeCompare(b.startTime));
    }
    return map;
  });

  ngOnInit(): void {
    this.dashSvc.getStudentDashboard().subscribe({
      next: d => {
        // We only get today's timetable from the API, but we show all slots grouped by day
        // For now display today's slots - full weekly requires a separate endpoint
        this.allSlots.set(d.todayTimetable ?? []);
        this.loading.set(false);
      },
      error: () => { this.loading.set(false); this.toast.error('Failed to load timetable'); },
    });
  }

  isToday(day: string): boolean {
    const days = ['SUN','MON','TUE','WED','THU','FRI','SAT'];
    return days[new Date().getDay()] === day;
  }
}
