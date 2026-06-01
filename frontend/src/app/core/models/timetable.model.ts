export interface TimetableSlotDto {
  id: string;
  classId: string;
  className: string;
  academicYearId: string;
  dayOfWeek: string;
  periodNo: number;
  subjectId: string | null;
  subjectName: string | null;
  teacherId: string | null;
  teacherName: string | null;
  startTime: string;
  endTime: string;
  roomNo: string | null;
}

export interface UpsertSlotRequest {
  classId: string;
  academicYearId: string;
  dayOfWeek: string;
  periodNo: number;
  subjectId: string | null;
  teacherId: string | null;
  startTime: string;
  endTime: string;
  roomNo: string | null;
}

export const DAYS_OF_WEEK = ['MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT'] as const;
export type DayOfWeek = typeof DAYS_OF_WEEK[number];
