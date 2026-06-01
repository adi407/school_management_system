import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface AttendanceSummary {
  studentId: string;
  studentName: string;
  totalDays: number;
  presentDays: number;
  absentDays: number;
  lateDays: number;
  attendancePercent: number;
}

export interface HomeworkItem {
  id: string;
  title: string;
  subjectName: string | null;
  className: string | null;
  dueDate: string;
  daysUntilDue: number;
  estimatedMinutes: number | null;
  description: string | null;
}

export interface TimetableSlot {
  id: string;
  dayOfWeek: string;
  periodNo: number;
  subjectName: string | null;
  teacherName: string | null;
  startTime: string;
  endTime: string;
  roomNo: string | null;
}

export interface StudentDashboard {
  studentId: string;
  studentName: string;
  admissionNo: string;
  rollNo: string | null;
  classId: string | null;
  className: string | null;
  attendance: AttendanceSummary;
  upcomingHomework: HomeworkItem[];
  todayTimetable: TimetableSlot[];
}

export interface FeeStructureItem { feeType: string; amount: number; dueDate: string | null; }
export interface FeePaymentItem   { feeType: string; amountPaid: number; paymentDate: string; paymentMode: string; receiptNo: string; }
export interface StudentFeesSummary {
  studentId: string;
  studentName: string;
  totalFees: number;
  totalPaid: number;
  balance: number;
  structures: FeeStructureItem[];
  payments: FeePaymentItem[];
}

export interface AnnouncementItem {
  id: string;
  title: string;
  body: string;
  targetRoles: string[];
  publishedAt: string;
  isPinned: boolean;
}

export interface ParentDashboard {
  studentId: string;
  studentName: string;
  admissionNo: string;
  rollNo: string | null;
  classId: string | null;
  className: string | null;
  guardianName: string;
  guardianRelation: string;
  attendance: AttendanceSummary;
  fees: StudentFeesSummary;
  upcomingHomework: HomeworkItem[];
  announcements: AnnouncementItem[];
}

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private http = inject(HttpClient);
  private base = environment.apiUrl;

  getStudentDashboard(): Observable<StudentDashboard> {
    return this.http.get<StudentDashboard>(`${this.base}/student/me`);
  }

  getParentDashboard(): Observable<ParentDashboard> {
    return this.http.get<ParentDashboard>(`${this.base}/parent/my-child`);
  }

  getAdminDashboard(): Observable<AdminDashboard> {
    return this.http.get<AdminDashboard>(`${this.base}/dashboard/admin`);
  }

  getTeacherDashboard(): Observable<TeacherDashboard> {
    return this.http.get<TeacherDashboard>(`${this.base}/teacher/me`);
  }
}

export interface AdminDashboard {
  totalStudents: number;
  activeStudents: number;
  attendanceToday: number;
  feeCollectedToday: number;
  totalFeePending: number;
  staffCount: number;
  upcomingExams: number;
  booksOverdue: number;
  recentActivity: { text: string; type: string; time: string }[];
}

export interface TeacherScheduleSlot {
  id: string; dayOfWeek: string; periodNo: number;
  subjectName: string | null; teacherName: string | null;
  startTime: string; endTime: string; roomNo: string | null;
  classId: string | null; className: string | null;
}

export interface TeacherDashboard {
  teacherName: string;
  todaySchedule: TeacherScheduleSlot[];
  pendingHomework: { id: string; title: string; className: string | null; dueDate: string; daysUntilDue: number }[];
  totalHomeworkAssigned: number;
  classesTeachingToday: number;
}
