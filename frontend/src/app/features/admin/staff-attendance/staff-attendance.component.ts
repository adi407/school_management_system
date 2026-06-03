import { Component, OnInit, signal, computed } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DatePipe, SlicePipe } from '@angular/common';
import { PayrollService } from '../../../core/services/payroll.service';
import { StaffService } from '../../../core/services/staff.service';
import { ToastService } from '../../../core/services/toast.service';
import { StaffAttendanceDto } from '../../../core/models/payroll.model';

interface AttendanceRow {
  staffId:   string;
  staffName: string;
  staffEmail: string;
  staffRole: string;
  status:    string;
  remarks:   string;
}

const STATUSES = ['PRESENT', 'ABSENT', 'HALF_DAY', 'LEAVE', 'HOLIDAY'];

@Component({
  selector: 'sms-staff-attendance',
  standalone: true,
  imports: [FormsModule, DatePipe, SlicePipe],
  templateUrl: './staff-attendance.component.html',
  styleUrls: ['./staff-attendance.component.scss'],
})
export class StaffAttendanceComponent implements OnInit {

  selectedDate = signal(new Date().toISOString().substring(0, 10));
  loading      = signal(false);
  saving       = signal(false);

  rows = signal<AttendanceRow[]>([]);
  saved = signal<StaffAttendanceDto[]>([]);

  readonly statuses = STATUSES;

  presentCount = computed(() => this.rows().filter(r => r.status === 'PRESENT').length);
  absentCount  = computed(() => this.rows().filter(r => r.status === 'ABSENT').length);
  halfDayCount = computed(() => this.rows().filter(r => r.status === 'HALF_DAY').length);

  constructor(
    private payrollService: PayrollService,
    private staffService: StaffService,
    private toast: ToastService,
  ) {}

  ngOnInit() {
    this.loadStaff();
  }

  // Load staff roster, then overlay any saved attendance for the selected date
  loadStaff() {
    this.loading.set(true);
    this.staffService.list().subscribe({
      next: (staff: any[]) => {
        const baseRows: AttendanceRow[] = staff.map(s => ({
          staffId:   s.id,
          staffName: `${s.firstName} ${s.lastName}`.trim(),
          staffEmail: s.email,
          staffRole: s.role,
          status:    'PRESENT',
          remarks:   '',
        }));
        this.rows.set(baseRows);
        this.loadDayRoll();
      },
      error: () => { this.loading.set(false); this.toast.error('Failed to load staff'); },
    });
  }

  onDateChange() {
    this.loadDayRoll();
  }

  loadDayRoll() {
    this.payrollService.getDayRoll(this.selectedDate()).subscribe({
      next: records => {
        // Overlay saved records onto the rows
        const map = new Map(records.map(r => [r.staffId, r]));
        this.rows.update(rows =>
          rows.map(row => {
            const saved = map.get(row.staffId);
            return saved
              ? { ...row, status: saved.status, remarks: saved.remarks ?? '' }
              : { ...row, status: 'PRESENT', remarks: '' };
          })
        );
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  markAll(status: string) {
    this.rows.update(rows => rows.map(r => ({ ...r, status })));
  }

  submit() {
    this.saving.set(true);
    this.payrollService.markStaffAttendance({
      date: this.selectedDate(),
      entries: this.rows().map(r => ({
        staffId: r.staffId,
        status:  r.status,
        remarks: r.remarks || null,
      })),
    }).subscribe({
      next: records => {
        this.saved.set(records);
        this.saving.set(false);
        this.toast.success(`Attendance saved for ${records.length} staff members`);
      },
      error: () => {
        this.saving.set(false);
        this.toast.error('Failed to save attendance');
      },
    });
  }

  statusClass(s: string): string {
    return ({
      PRESENT:  'status-btn--present',
      ABSENT:   'status-btn--absent',
      HALF_DAY: 'status-btn--half',
      LEAVE:    'status-btn--leave',
      HOLIDAY:  'status-btn--holiday',
    } as Record<string,string>)[s] ?? '';
  }

  statusLabel(s: string): string {
    return ({ PRESENT:'P', ABSENT:'A', HALF_DAY:'½', LEAVE:'L', HOLIDAY:'H' } as Record<string,string>)[s] ?? s;
  }

  updateStatus(index: number, status: string) {
    this.rows.update(r => r.map((row, j) => j === index ? { ...row, status } : row));
  }

  updateRemarks(index: number, remarks: string) {
    this.rows.update(r => r.map((row, j) => j === index ? { ...row, remarks } : row));
  }
}
