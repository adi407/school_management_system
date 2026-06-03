export type StaffModule =
  | 'TEACHING'
  | 'EXAMINATIONS'
  | 'ACCOUNTING'
  | 'PAYROLL'
  | 'HR'
  | 'LIBRARY'
  | 'TRANSPORT'
  | 'HOSTEL'
  | 'ADMISSIONS'
  | 'ANNOUNCEMENTS'
  | 'WELLNESS'
  | 'SCHOOL_SETUP';

/** Returned by GET /api/v1/my-modules — loaded once at login */
export interface MyModuleDto {
  module: StaffModule;
  subPermissions: string[] | null; // null → full access
}

/** Used by admin assignment page */
export interface StaffModuleAssignmentDto {
  assignmentId: string;
  staffId: string;
  staffName: string;
  staffEmail: string;
  module: StaffModule;
  subPermissions: string[] | null;
  isActive: boolean;
  assignedAt: string;
}

export interface AssignModuleRequest {
  module: StaffModule;
  subPermissions?: string[];
}

/** Returned by GET /api/v1/super-admin/schools/:id/staff */
export interface SchoolUserDto {
  id: string;
  fullName: string;
  email: string;
  role: string;
  isActive: boolean;
}

// ── Display metadata ───────────────────────────────────────────────────────────

export const MODULE_LABELS: Record<StaffModule, string> = {
  TEACHING:      'Teaching',
  EXAMINATIONS:  'Examinations',
  ACCOUNTING:    'Accounting',
  PAYROLL:       'Payroll',
  HR:            'HR & Staff',
  LIBRARY:       'Library',
  TRANSPORT:     'Transport',
  HOSTEL:        'Hostel',
  ADMISSIONS:    'Admissions',
  ANNOUNCEMENTS: 'Announcements',
  WELLNESS:      'Wellness',
  SCHOOL_SETUP:  'School Setup',
};

export const MODULE_ICONS: Record<StaffModule, string> = {
  TEACHING:      '📖',
  EXAMINATIONS:  '📋',
  ACCOUNTING:    '💰',
  PAYROLL:       '💼',
  HR:            '👥',
  LIBRARY:       '📚',
  TRANSPORT:     '🚌',
  HOSTEL:        '🏠',
  ADMISSIONS:    '🎓',
  ANNOUNCEMENTS: '📢',
  WELLNESS:      '💙',
  SCHOOL_SETUP:  '⚙️',
};

/**
 * Which sub-permissions belong to each module.
 * Used to render checkboxes in the assignment UI.
 */
export const MODULE_PERMISSIONS: Partial<Record<StaffModule, string[]>> = {
  TEACHING: [
    'TEACHING__MARK_ATTENDANCE',
    'TEACHING__MANAGE_HOMEWORK',
    'TEACHING__MANAGE_TIMETABLE',
  ],
  EXAMINATIONS: [
    'EXAMINATIONS__SCHEDULE_EXAMS',
    'EXAMINATIONS__ENTER_MARKS',
    'EXAMINATIONS__GENERATE_REPORT_CARDS',
  ],
  ACCOUNTING: [
    'ACCOUNTING__COLLECT_FEES',
    'ACCOUNTING__MANAGE_FEE_STRUCTURES',
    'ACCOUNTING__LOG_EXPENSES',
    'ACCOUNTING__VIEW_REPORTS',
  ],
  PAYROLL: [
    'PAYROLL__MANAGE_SALARY_STRUCTURES',
    'PAYROLL__RUN_PAYROLL',
    'PAYROLL__APPROVE_PAYROLL',
    'PAYROLL__MARK_PAID',
    'PAYROLL__VIEW_PAYSLIPS',
  ],
  HR: [
    'HR__MARK_STAFF_ATTENDANCE',
    'HR__MANAGE_STAFF',
  ],
  LIBRARY: [
    'LIBRARY__MANAGE_CATALOG',
    'LIBRARY__MANAGE_LENDING',
  ],
  TRANSPORT: [
    'TRANSPORT__MANAGE_ROUTES',
    'TRANSPORT__MANAGE_VEHICLES',
    'TRANSPORT__ASSIGN_STUDENTS',
  ],
  HOSTEL: [
    'HOSTEL__MANAGE_ROOMS',
    'HOSTEL__MANAGE_RESIDENTS',
    'HOSTEL__MARK_HOSTEL_ATTENDANCE',
  ],
  ADMISSIONS: [
    'ADMISSIONS__ENROLL_STUDENTS',
    'ADMISSIONS__ISSUE_TC',
    'ADMISSIONS__MANAGE_GUARDIANS',
  ],
  ANNOUNCEMENTS: ['ANNOUNCEMENTS__POST'],
  WELLNESS:      ['WELLNESS__VIEW_DATA'],
  SCHOOL_SETUP: [
    'SCHOOL_SETUP__MANAGE_ACADEMIC_YEARS',
    'SCHOOL_SETUP__MANAGE_CLASSES',
    'SCHOOL_SETUP__MANAGE_SUBJECTS',
    'SCHOOL_SETUP__MANAGE_SCHOOL_PROFILE',
    'SCHOOL_SETUP__ASSIGN_MODULES',
  ],
};

/** Human-readable label for a sub-permission key */
export function formatPermissionLabel(key: string): string {
  // e.g. "PAYROLL__RUN_PAYROLL" → "Run Payroll"
  const parts = key.split('__');
  const action = parts[1] ?? key;
  return action.split('_').map(w => w[0] + w.slice(1).toLowerCase()).join(' ');
}
