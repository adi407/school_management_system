export type AttendanceStatus = 'PRESENT' | 'ABSENT' | 'LATE' | 'HOLIDAY' | 'EXCUSED';

export interface AttendanceRecordDto {
  id: string;
  studentId: string;
  studentName: string;
  admissionNo: string;
  classId: string;
  className: string;
  attendanceDate: string;
  status: AttendanceStatus;
  remarks: string | null;
  markedById: string;
  markedByEmail: string;
}

export interface AttendanceSummaryDto {
  studentId: string;
  studentName: string;
  totalDays: number;
  presentDays: number;
  absentDays: number;
  lateDays: number;
  attendancePercent: number;
}

export interface AttendanceEntryRequest {
  studentId: string;
  status: AttendanceStatus;
  remarks?: string;
}

export interface MarkAttendanceRequest {
  classId: string;
  date: string;
  entries: AttendanceEntryRequest[];
}
