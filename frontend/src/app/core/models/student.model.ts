// Aligned with backend StudentSummaryDto / StudentDto / GuardianDto

export type Gender = 'MALE' | 'FEMALE' | 'OTHER';
// Backend uses GEN (not GENERAL)
export type StudentCategory = 'GEN' | 'OBC' | 'SC' | 'ST' | 'EWS';
export type AttendanceStatus = 'PRESENT' | 'ABSENT' | 'LATE' | 'EXCUSED';

export interface GuardianDto {
  id: string;
  name: string;
  relation: string;
  phone: string;
  email?: string;
  aadhaarNo?: string;
  occupation?: string;
  address?: string;
  isPrimary: boolean;
  isAuthorizedPickup: boolean;
  photoUrl?: string;
}

/** Lightweight — returned in paginated list */
export interface StudentSummaryDto {
  id: string;
  admissionNo: string;
  rollNo?: string;
  firstName: string;
  lastName: string;
  fullName: string;
  dateOfBirth: string;
  gender: Gender;
  category: StudentCategory;
  classId?: string;
  className?: string;
  isActive: boolean;
  admissionDate: string;
  photoUrl?: string;
  createdAt: string;
}

/** Full profile — returned by GET /students/{id} */
export interface StudentDto extends StudentSummaryDto {
  schoolId: string;
  bloodGroup?: string;
  nationality?: string;
  religion?: string;
  caste?: string;
  motherTongue?: string;
  aadhaarNo?: string;
  academicYearId?: string;
  academicYearName?: string;
  houseGroup?: string;
  tcIssued: boolean;
  medicalConditions?: string;
  guardians: GuardianDto[];
  updatedAt: string;
}

export interface CreateGuardianRequest {
  name: string;
  relation: string;
  phone: string;
  email?: string;
  aadhaarNo?: string;
  occupation?: string;
  address?: string;
  isPrimary: boolean;
  isAuthorizedPickup: boolean;
}

export interface CreateStudentRequest {
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  gender: Gender;
  admissionDate: string;
  classId?: string;
  academicYearId?: string;
  rollNo?: string;
  admissionNo?: string;
  bloodGroup?: string;
  nationality?: string;
  religion?: string;
  caste?: string;
  category?: StudentCategory;
  motherTongue?: string;
  aadhaarNo?: string;
  houseGroup?: string;
  medicalConditions?: string;
  photoUrl?: string;
  guardians: CreateGuardianRequest[];
}

export interface UpdateStudentRequest {
  firstName?: string;
  lastName?: string;
  dateOfBirth?: string;
  gender?: Gender;
  bloodGroup?: string;
  nationality?: string;
  religion?: string;
  caste?: string;
  category?: StudentCategory;
  motherTongue?: string;
  aadhaarNo?: string;
  classId?: string;
  academicYearId?: string;
  rollNo?: string;
  houseGroup?: string;
  medicalConditions?: string;
  photoUrl?: string;
}

export interface StudentFilter {
  search?: string;
  classId?: string;
  gender?: Gender;
  category?: StudentCategory;
  isActive?: boolean;
  page?: number;
  size?: number;
  sort?: string;
}

export interface AttendanceSummary {
  totalDays: number;
  presentDays: number;
  absentDays: number;
  lateDays: number;
  percentage: number;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
