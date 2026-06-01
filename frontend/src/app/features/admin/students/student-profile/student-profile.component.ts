import { Component, signal, computed, OnInit, inject } from '@angular/core';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { DatePipe, DecimalPipe } from '@angular/common';
import { StudentDto, AttendanceSummary } from '../../../../core/models/student.model';
import { StudentService } from '../../../../core/services/student.service';

type Tab = 'overview' | 'attendance' | 'fees' | 'exams' | 'documents';

@Component({
  selector: 'sms-student-profile',
  standalone: true,
  imports: [RouterModule, DatePipe, DecimalPipe],
  templateUrl: './student-profile.component.html',
  styleUrls: ['./student-profile.component.scss'],
})
export class StudentProfileComponent implements OnInit {
  private route          = inject(ActivatedRoute);
  private studentService = inject(StudentService);

  activeTab = signal<Tab>('overview');
  loading   = signal(true);
  error     = signal('');

  tabs: { key: Tab; label: string; icon: string }[] = [
    { key: 'overview',   label: 'Overview',   icon: '👤' },
    { key: 'attendance', label: 'Attendance', icon: '✓'  },
    { key: 'fees',       label: 'Fees',       icon: '💰' },
    { key: 'exams',      label: 'Exams',      icon: '📝' },
    { key: 'documents',  label: 'Documents',  icon: '📄' },
  ];

  student = signal<StudentDto | null>(null);

  // ── Attendance (mock until attendance API is built) ───────────────
  attendanceSummary = signal<AttendanceSummary>({
    totalDays: 0, presentDays: 0, absentDays: 0, lateDays: 0, percentage: 0
  });

  attendanceHistory = signal<{ date: string; status: string; subject: string }[]>([]);

  // ── Fees (mock until fee API is built) ───────────────────────────
  feeTransactions = signal<{ id: string; description: string; amount: number; paid: number; date: string; status: string }[]>([]);

  totalFee     = computed(() => this.feeTransactions().reduce((s, t) => s + t.amount, 0));
  totalPaid    = computed(() => this.feeTransactions().reduce((s, t) => s + t.paid,   0));
  totalPending = computed(() => this.totalFee() - this.totalPaid());

  // ── Exams (mock until exam API is built) ─────────────────────────
  examResults = signal<{ subject: string; marks: number; maxMarks: number; grade: string; examName: string }[]>([]);

  avgMarks = computed(() => {
    const r = this.examResults();
    return r.length ? r.reduce((s, x) => s + x.marks, 0) / r.length : 0;
  });

  // ── Documents (mock until document API is built) ──────────────────
  documents = signal<{ name: string; type: string; uploaded: string; verified: boolean }[]>([]);

  // ── Guardian convenience ──────────────────────────────────────────
  primaryGuardian = computed(() =>
    this.student()?.guardians?.find(g => g.isPrimary) ?? this.student()?.guardians?.[0]
  );

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) { this.error.set('No student ID provided'); this.loading.set(false); return; }

    this.studentService.get(id).subscribe({
      next: student => {
        this.student.set(student);
        this.loading.set(false);
        this.loadMockSubData();
      },
      error: err => {
        this.error.set(err.error?.message ?? 'Student not found');
        this.loading.set(false);
      },
    });
  }

  /** Load placeholder data for tabs not yet wired to real APIs */
  private loadMockSubData() {
    this.attendanceSummary.set({
      totalDays: 120, presentDays: 108, absentDays: 9, lateDays: 3, percentage: 90
    });
    this.attendanceHistory.set([
      { date: '2026-05-27', status: 'PRESENT', subject: 'All' },
      { date: '2026-05-26', status: 'ABSENT',  subject: 'All' },
      { date: '2026-05-23', status: 'PRESENT', subject: 'All' },
      { date: '2026-05-22', status: 'LATE',    subject: 'All' },
    ]);
    this.feeTransactions.set([
      { id: 'TXN-001', description: 'Tuition Fee – Term 1', amount: 25000, paid: 25000, date: '2026-04-01', status: 'PAID'    },
      { id: 'TXN-002', description: 'Lab Fee',              amount: 3000,  paid: 0,     date: '2026-04-15', status: 'PENDING' },
    ]);
    this.examResults.set([
      { subject: 'Mathematics', marks: 87, maxMarks: 100, grade: 'A',  examName: 'Mid-Term 1' },
      { subject: 'Science',     marks: 91, maxMarks: 100, grade: 'A+', examName: 'Mid-Term 1' },
      { subject: 'English',     marks: 78, maxMarks: 100, grade: 'B+', examName: 'Mid-Term 1' },
    ]);
    this.documents.set([
      { name: 'Birth Certificate',    type: 'PDF', uploaded: '2026-06-01', verified: true  },
      { name: 'Transfer Certificate', type: 'PDF', uploaded: '2026-06-01', verified: true  },
      { name: 'Aadhar Card',          type: 'IMG', uploaded: '2026-06-02', verified: false },
    ]);
  }

  setTab(tab: Tab) { this.activeTab.set(tab); }

  attendanceColor(status: string) {
    const map: Record<string, string> = { PRESENT: 'green', ABSENT: 'red', LATE: 'orange', EXCUSED: 'blue' };
    return map[status] ?? 'gray';
  }

  gradeColor(grade: string) {
    if (grade.startsWith('A')) return 'green';
    if (grade.startsWith('B')) return 'blue';
    if (grade.startsWith('C')) return 'yellow';
    return 'red';
  }
}
