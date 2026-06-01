export interface HomeworkDto {
  id: string;
  schoolId: string;
  classId: string | null;
  className: string | null;
  subjectId: string | null;
  subjectName: string | null;
  teacherId: string;
  teacherEmail: string;
  title: string;
  description: string;
  dueDate: string;          // LocalDate from backend → "2026-06-15"
  estimatedMinutes: number | null;
  attachments: string;      // JSON array string
  isPublished: boolean;
  isSchoolWide: boolean;    // true = broadcast to all classes
  daysUntilDue: number;     // negative = overdue
  createdAt: string;
  updatedAt: string;
}

export interface CreateHomeworkRequest {
  isSchoolWide: boolean;
  classId?: string;         // omitted when isSchoolWide = true
  subjectId?: string;
  academicYearId?: string;
  title: string;
  description: string;
  dueDate: string;          // ISO date "2026-06-15"
  estimatedMinutes?: number;
  isPublished: boolean;
}

export interface HomeworkFilter {
  classId?: string;
  subjectId?: string;
  from?: string;
  to?: string;
  page?: number;
  size?: number;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
