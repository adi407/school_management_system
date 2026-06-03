package com.sms.core.enums;

/**
 * Fine-grained sub-permissions within a module.
 *
 * When a staff_module_assignment row has sub_permissions = null  → full module access.
 * When sub_permissions is a non-empty JSON array → only the listed permissions are granted.
 *
 * Convention: "<MODULE>__<ACTION>" so a permission is self-describing.
 */
public enum ModulePermission {

    // ── TEACHING ─────────────────────────────────────────────────────────────
    TEACHING__MARK_ATTENDANCE,
    TEACHING__MANAGE_HOMEWORK,
    TEACHING__MANAGE_TIMETABLE,

    // ── EXAMINATIONS ─────────────────────────────────────────────────────────
    EXAMINATIONS__SCHEDULE_EXAMS,
    EXAMINATIONS__ENTER_MARKS,
    EXAMINATIONS__GENERATE_REPORT_CARDS,

    // ── ACCOUNTING ───────────────────────────────────────────────────────────
    ACCOUNTING__COLLECT_FEES,
    ACCOUNTING__MANAGE_FEE_STRUCTURES,
    ACCOUNTING__LOG_EXPENSES,
    ACCOUNTING__VIEW_REPORTS,

    // ── PAYROLL ──────────────────────────────────────────────────────────────
    PAYROLL__MANAGE_SALARY_STRUCTURES,
    PAYROLL__RUN_PAYROLL,
    PAYROLL__APPROVE_PAYROLL,
    PAYROLL__MARK_PAID,
    PAYROLL__VIEW_PAYSLIPS,

    // ── HR ───────────────────────────────────────────────────────────────────
    HR__MARK_STAFF_ATTENDANCE,
    HR__MANAGE_STAFF,

    // ── LIBRARY ──────────────────────────────────────────────────────────────
    LIBRARY__MANAGE_CATALOG,
    LIBRARY__MANAGE_LENDING,

    // ── TRANSPORT ────────────────────────────────────────────────────────────
    TRANSPORT__MANAGE_ROUTES,
    TRANSPORT__MANAGE_VEHICLES,
    TRANSPORT__ASSIGN_STUDENTS,

    // ── HOSTEL ───────────────────────────────────────────────────────────────
    HOSTEL__MANAGE_ROOMS,
    HOSTEL__MANAGE_RESIDENTS,
    HOSTEL__MARK_HOSTEL_ATTENDANCE,

    // ── ADMISSIONS ───────────────────────────────────────────────────────────
    ADMISSIONS__ENROLL_STUDENTS,
    ADMISSIONS__ISSUE_TC,
    ADMISSIONS__MANAGE_GUARDIANS,

    // ── ANNOUNCEMENTS ────────────────────────────────────────────────────────
    ANNOUNCEMENTS__POST,

    // ── WELLNESS ─────────────────────────────────────────────────────────────
    WELLNESS__VIEW_DATA,

    // ── SCHOOL_SETUP ─────────────────────────────────────────────────────────
    SCHOOL_SETUP__MANAGE_ACADEMIC_YEARS,
    SCHOOL_SETUP__MANAGE_CLASSES,
    SCHOOL_SETUP__MANAGE_SUBJECTS,
    SCHOOL_SETUP__MANAGE_SCHOOL_PROFILE,
    SCHOOL_SETUP__ASSIGN_MODULES
}
