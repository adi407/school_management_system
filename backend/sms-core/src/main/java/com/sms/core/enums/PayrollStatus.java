package com.sms.core.enums;

public enum PayrollStatus {
    DRAFT,      // computed but not approved
    APPROVED,   // approved by admin, ready to pay
    PAID        // salaries disbursed
}
