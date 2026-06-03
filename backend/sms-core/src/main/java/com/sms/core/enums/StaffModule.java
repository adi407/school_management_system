package com.sms.core.enums;

/**
 * Every functional area of the school system.
 * A staff member gains access to a module only when a SCHOOL_SETUP holder
 * explicitly assigns it — no implicit access from role alone.
 *
 * SUPER_ADMIN bypasses all module checks.
 * The founding SCHOOL_ADMIN of each school is auto-assigned ALL modules.
 */
public enum StaffModule {

    TEACHING,        // Student attendance, homework, timetable
    EXAMINATIONS,    // Exam scheduling, mark entry, report cards
    ACCOUNTING,      // Fee collection, fee structures, expense logging
    PAYROLL,         // Salary structures, payroll runs, payslip viewing
    HR,              // Staff management, staff attendance marking
    LIBRARY,         // Book catalog, lending & returns
    TRANSPORT,       // Routes, vehicles, student assignment
    HOSTEL,          // Room allocation, hostel attendance
    ADMISSIONS,      // Student enrollment, TC issuance, guardians
    ANNOUNCEMENTS,   // Post school-wide notices
    WELLNESS,        // Campus Pulse wellness data
    SCHOOL_SETUP     // Academic setup + module assignment (privileged)
}
