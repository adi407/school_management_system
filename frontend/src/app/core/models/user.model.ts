export type Role =
  | 'SUPER_ADMIN'
  | 'SCHOOL_ADMIN'
  | 'TEACHER'
  | 'STUDENT'
  | 'PARENT'
  | 'ACCOUNTANT'
  | 'LIBRARIAN'
  | 'TRANSPORT_MANAGER'
  | 'HOSTEL_WARDEN';

export type FeatureKey =
  | 'STUDENT_MANAGEMENT' | 'ATTENDANCE' | 'ACADEMIC_MANAGEMENT' | 'NOTICE_BOARD'
  | 'EXAM_MANAGEMENT' | 'FEE_MANAGEMENT' | 'ONLINE_FEE_PAYMENT' | 'STAFF_HR'
  | 'PAYROLL' | 'PARENT_PORTAL' | 'TIMETABLE' | 'HOMEWORK'
  | 'DOCUMENT_MANAGEMENT' | 'REPORT_CARDS' | 'LIBRARY' | 'TRANSPORT'
  | 'GPS_TRACKING' | 'HOSTEL' | 'EXTRA_CURRICULAR' | 'ACHIEVEMENTS'
  | 'ONLINE_CLASSES' | 'ASSIGNMENT_PORTAL' | 'ANALYTICS_DASHBOARD'
  | 'CUSTOM_REPORTS' | 'BULK_SMS' | 'WHATSAPP_INTEGRATION'
  | 'FACE_RECOGNITION_ATTENDANCE' | 'MULTI_BRANCH' | 'API_ACCESS'
  | 'WHITE_LABELLING' | 'GRIEVANCE_PORTAL' | 'ALUMNI_MODULE';

export interface UserInfo {
  id: string;
  schoolId: string | null;
  email: string;
  fullName: string;
  role: Role;
  profilePhotoUrl: string | null;
  enabledFeatures: string[];
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  user: UserInfo;
}
