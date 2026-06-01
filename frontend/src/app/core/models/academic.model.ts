export interface AcademicYearDto {
  id: string;
  schoolId: string;
  name: string;
  startDate: string;
  endDate: string;
  isCurrent: boolean;
  createdAt: string;
}

export interface ClassDto {
  id: string;
  schoolId: string;
  name: string;
  grade: number;
  section: string;
  capacity: number;
  studentCount: number;
  classTeacherId: string | null;
  classTeacherName: string | null;
  createdAt: string;
}

export interface SubjectDto {
  id: string;
  schoolId: string;
  name: string;
  code: string;
  type: string;  // CORE | ELECTIVE | ACTIVITY | LANGUAGE
  creditHours: number | null;
  createdAt: string;
}

export interface CreateClassRequest {
  grade: number;
  section: string;
  name: string;
  capacity?: number;
  classTeacherId?: string;
}

export interface CreateSubjectRequest {
  name: string;
  code: string;
  type?: string;
  creditHours?: number;
}
