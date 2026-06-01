package com.sms.core.enums;

import java.util.Set;

public enum SubscriptionTier {
    FREE,
    BASIC,
    PREMIUM,
    ENTERPRISE;

    public Set<FeatureKey> defaultFeatures() {
        return switch (this) {
            case FREE -> Set.of(
                FeatureKey.STUDENT_MANAGEMENT,
                FeatureKey.ATTENDANCE,
                FeatureKey.ACADEMIC_MANAGEMENT,
                FeatureKey.NOTICE_BOARD
            );
            case BASIC -> Set.of(
                FeatureKey.STUDENT_MANAGEMENT, FeatureKey.ATTENDANCE,
                FeatureKey.ACADEMIC_MANAGEMENT, FeatureKey.NOTICE_BOARD,
                FeatureKey.EXAM_MANAGEMENT, FeatureKey.FEE_MANAGEMENT,
                FeatureKey.TIMETABLE, FeatureKey.HOMEWORK, FeatureKey.REPORT_CARDS
            );
            case PREMIUM -> Set.of(
                FeatureKey.STUDENT_MANAGEMENT, FeatureKey.ATTENDANCE,
                FeatureKey.ACADEMIC_MANAGEMENT, FeatureKey.NOTICE_BOARD,
                FeatureKey.EXAM_MANAGEMENT, FeatureKey.FEE_MANAGEMENT,
                FeatureKey.ONLINE_FEE_PAYMENT, FeatureKey.STAFF_HR,
                FeatureKey.PAYROLL, FeatureKey.PARENT_PORTAL,
                FeatureKey.TIMETABLE, FeatureKey.HOMEWORK,
                FeatureKey.DOCUMENT_MANAGEMENT, FeatureKey.REPORT_CARDS,
                FeatureKey.LIBRARY, FeatureKey.TRANSPORT, FeatureKey.GPS_TRACKING,
                FeatureKey.HOSTEL, FeatureKey.EXTRA_CURRICULAR, FeatureKey.ACHIEVEMENTS,
                FeatureKey.ONLINE_CLASSES, FeatureKey.ASSIGNMENT_PORTAL
            );
            case ENTERPRISE -> Set.of(FeatureKey.values());
        };
    }
}
