import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { AttendanceService } from '../../../core/services/attendance.service';
import { StudentService } from '../../../core/services/student.service';
import { AcademicService } from '../../../core/services/academic.service';
import { ToastService } from '../../../core/services/toast.service';
import { ClassDto } from '../../../core/models/academic.model';
import { AttendanceStatus, AttendanceEntryRequest } from '../../../core/models/attendance.model';

interface AttendanceRow {
  studentId:   string;
  name:        string;
  admissionNo: string;
  status:      AttendanceStatus;
  remarks:     string;
}

@Component({
  selector: 'sms-attendance',
  standalone: true,
  imports: [FormsModule, DatePipe],
  templateUrl: './attendance.component.html',
  styleUrls: ['./attendance.component.scss'],
})
export class AttendanceComponent implements OnInit {
  private attendanceService = inject(AttendanceService);
  private studentService    = inject(StudentService);
  private academicService   = inject(AcademicService);
  private toast             = inject(ToastService);

  today         = new Date();
  classes       = signal<ClassDto[]>([]);
  selectedClass = signal('');
  selectedDate  = signal(new Date().toISOString().split('T')[0]);
  loading       = signal(false);
  saving        = signal(false);
  rollLoaded    = signal(false);

  rows = signal<AttendanceRow[]>([]);

  presentCount = computed(() => this.rows().filter(r => r.status === 'PRESENT').length);
  absentCount  = computed(() => this.rows().filter(r => r.status === 'ABSENT').length);
  lateCount    = computed(() => this.rows().filter(r => r.status === 'LATE').length);
  percentage   = computed(() =>
    this.rows().length ? Math.round((this.presentCount() + this.lateCount()) / this.rows().length * 100) : 0
  );

  ngOnInit() {
    this.academicService.listClasses().subscribe(c => this.classes.set(c));
  }

  loadRoll() {
    if (!this.selectedClass() || !this.selectedDate()) return;
    this.loading.set(true);
    this.rollLoaded.set(false);
    this.rows.set([]);

    // Try loading existing attendance first
    this.attendanceService.getClassRoll(this.selectedClass(), this.selectedDate()).subscribe({
      next: existing => {
        if (existing.length > 0) {
          this.rows.set(existing.map(r => ({
            studentId: r.studentId, name: r.studentName,
            admissionNo: r.admissionNo, status: r.status, remarks: r.remarks ?? '',
          })));
          this.loading.set(false);
          this.rollLoaded.set(true);
        } else {
          // No attendance yet — load class students and default to PRESENT
          this.studentService.list({ classId: this.selectedClass(), size: 200 }).subscribe({
            next: page => {
              this.rows.set(page.content.map(s => ({
                studentId:   s.id,
                name:        s.firstName + ' ' + s.lastName,
                admissionNo: s.admissionNo,
                status:      'PRESENT' as AttendanceStatus,
                remarks:     '',
              })));
              this.loading.set(false);
              this.rollLoaded.set(true);
            },
            error: () => { this.loading.set(false); this.toast.error('Failed to load students'); }
          });
        }
      },
      error: () => { this.loading.set(false); this.toast.error('Failed to load roll'); }
    });
  }

  setStatus(studentId: string, status: AttendanceStatus) {
    this.rows.update(rows => rows.map(r => r.studentId === studentId ? { ...r, status } : r));
  }

  markAll(status: AttendanceStatus) {
    this.rows.update(rows => rows.map(r => ({ ...r, status })));
  }

  save() {
    if (!this.selectedClass() || this.rows().length === 0) return;
    this.saving.set(true);

    const entries: AttendanceEntryRequest[] = this.rows().map(r => ({
      studentId: r.studentId,
      status:    r.status,
      remarks:   r.remarks || undefined,
    }));

    this.attendanceService.mark({ classId: this.selectedClass(), date: this.selectedDate(), entries }).subscribe({
      next: () => {
        this.saving.set(false);
        this.toast.success(`Attendance saved — ${this.presentCount()} present, ${this.absentCount()} absent`);
      },
      error: err => {
        this.saving.set(false);
        this.toast.error(err?.error?.message ?? 'Failed to save');
      }
    });
  }
}
