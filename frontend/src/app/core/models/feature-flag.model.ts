import { FeatureKey } from './user.model';

export interface FeatureFlagDto {
  featureKey: FeatureKey;
  isEnabled: boolean;
  config: string | null;
  updatedAt: string | null;
}

export type FeatureFlagsMap = Record<string, FeatureFlagDto>;

export const FEATURE_GROUPS: { label: string; keys: FeatureKey[] }[] = [
  {
    label: 'Core (Always On)',
    keys: ['STUDENT_MANAGEMENT', 'ATTENDANCE', 'ACADEMIC_MANAGEMENT', 'NOTICE_BOARD'],
  },
  {
    label: 'Standard',
    keys: [
      'EXAM_MANAGEMENT', 'FEE_MANAGEMENT', 'ONLINE_FEE_PAYMENT', 'STAFF_HR',
      'PAYROLL', 'PARENT_PORTAL', 'TIMETABLE', 'HOMEWORK',
      'DOCUMENT_MANAGEMENT', 'REPORT_CARDS',
    ],
  },
  {
    label: 'Advanced',
    keys: [
      'LIBRARY', 'TRANSPORT', 'GPS_TRACKING', 'HOSTEL',
      'EXTRA_CURRICULAR', 'ACHIEVEMENTS', 'ONLINE_CLASSES', 'ASSIGNMENT_PORTAL',
    ],
  },
  {
    label: 'Premium',
    keys: [
      'ANALYTICS_DASHBOARD', 'CUSTOM_REPORTS', 'BULK_SMS', 'WHATSAPP_INTEGRATION',
      'FACE_RECOGNITION_ATTENDANCE', 'MULTI_BRANCH', 'API_ACCESS',
      'WHITE_LABELLING', 'GRIEVANCE_PORTAL', 'ALUMNI_MODULE',
    ],
  },
];
