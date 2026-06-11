-- ============================================================================
-- V12: Create payroll & staff attendance tables (entities exist, no migration)
-- ============================================================================

-- ── 1. salary_structures ──────────────────────────────────────────────────
CREATE TABLE salary_structures (
    id                          UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id                   UUID           NOT NULL REFERENCES schools(id),
    user_id                     UUID           NOT NULL REFERENCES users(id),
    basic_salary                NUMERIC(12,2)  NOT NULL DEFAULT 0,
    hra_amount                  NUMERIC(12,2)  DEFAULT 0,
    da_amount                   NUMERIC(12,2)  DEFAULT 0,
    ta_amount                   NUMERIC(12,2)  DEFAULT 0,
    medical_allowance           NUMERIC(12,2)  DEFAULT 0,
    other_allowances            NUMERIC(12,2)  DEFAULT 0,
    pf_enrolled                 BOOLEAN        NOT NULL DEFAULT TRUE,
    pf_wage_ceiling             NUMERIC(12,2)  DEFAULT 15000.00,
    tax_regime                  VARCHAR(10)    NOT NULL DEFAULT 'NEW',
    declared80c                 NUMERIC(12,2)  DEFAULT 0,
    declared_hra_exemption      NUMERIC(12,2)  DEFAULT 0,
    declared_other_exemptions   NUMERIC(12,2)  DEFAULT 0,
    effective_from              DATE           NOT NULL,
    effective_to                DATE,
    is_active                   BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at                  TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at                  TIMESTAMPTZ
);
CREATE INDEX idx_sal_struct_school ON salary_structures(school_id);
CREATE INDEX idx_sal_struct_user   ON salary_structures(user_id);

-- ── 2. payroll_runs ───────────────────────────────────────────────────────
CREATE TABLE payroll_runs (
    id                     UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id              UUID           NOT NULL REFERENCES schools(id),
    run_month              INT            NOT NULL,
    run_year               INT            NOT NULL,
    total_working_days     INT            NOT NULL DEFAULT 26,
    status                 VARCHAR(12)    NOT NULL DEFAULT 'DRAFT',
    total_gross            NUMERIC(14,2)  DEFAULT 0,
    total_pf_employee      NUMERIC(14,2)  DEFAULT 0,
    total_pf_employer      NUMERIC(14,2)  DEFAULT 0,
    total_esi_employee     NUMERIC(14,2)  DEFAULT 0,
    total_esi_employer     NUMERIC(14,2)  DEFAULT 0,
    total_professional_tax NUMERIC(14,2)  DEFAULT 0,
    total_tds              NUMERIC(14,2)  DEFAULT 0,
    total_lop_deduction    NUMERIC(14,2)  DEFAULT 0,
    total_net_payout       NUMERIC(14,2)  DEFAULT 0,
    triggered_by           UUID           REFERENCES users(id),
    approved_by            UUID           REFERENCES users(id),
    approved_at            TIMESTAMPTZ,
    paid_at                TIMESTAMPTZ,
    notes                  VARCHAR(1000),
    created_at             TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at             TIMESTAMPTZ,
    CONSTRAINT uq_payroll_run_month UNIQUE (school_id, run_month, run_year)
);
CREATE INDEX idx_payroll_run_school ON payroll_runs(school_id);
CREATE INDEX idx_payroll_run_month  ON payroll_runs(school_id, run_month, run_year);

-- ── 3. payslips ───────────────────────────────────────────────────────────
CREATE TABLE payslips (
    id                   UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id            UUID           NOT NULL REFERENCES schools(id),
    payroll_run_id       UUID           NOT NULL REFERENCES payroll_runs(id),
    user_id              UUID           NOT NULL REFERENCES users(id),
    salary_structure_id  UUID           REFERENCES salary_structures(id),
    basic_salary         NUMERIC(12,2)  DEFAULT 0,
    hra_amount           NUMERIC(12,2)  DEFAULT 0,
    da_amount            NUMERIC(12,2)  DEFAULT 0,
    ta_amount            NUMERIC(12,2)  DEFAULT 0,
    medical_allowance    NUMERIC(12,2)  DEFAULT 0,
    other_allowances     NUMERIC(12,2)  DEFAULT 0,
    gross_salary         NUMERIC(12,2)  DEFAULT 0,
    total_working_days   INT            NOT NULL DEFAULT 26,
    present_days         INT            NOT NULL DEFAULT 26,
    lop_days             INT            NOT NULL DEFAULT 0,
    lop_deduction        NUMERIC(12,2)  DEFAULT 0,
    effective_gross      NUMERIC(12,2)  DEFAULT 0,
    pf_employee          NUMERIC(12,2)  DEFAULT 0,
    pf_employer          NUMERIC(12,2)  DEFAULT 0,
    esi_employee         NUMERIC(12,2)  DEFAULT 0,
    esi_employer         NUMERIC(12,2)  DEFAULT 0,
    professional_tax     NUMERIC(12,2)  DEFAULT 0,
    tds                  NUMERIC(12,2)  DEFAULT 0,
    total_deductions     NUMERIC(12,2)  DEFAULT 0,
    net_salary           NUMERIC(12,2)  DEFAULT 0,
    created_at           TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ,
    CONSTRAINT uq_payslip_run_staff UNIQUE (payroll_run_id, user_id)
);
CREATE INDEX idx_payslip_school    ON payslips(school_id);
CREATE INDEX idx_payslip_run       ON payslips(payroll_run_id);
CREATE INDEX idx_payslip_staff     ON payslips(user_id);
CREATE INDEX idx_payslip_run_staff ON payslips(payroll_run_id, user_id);

-- ── 4. staff_attendance ───────────────────────────────────────────────────
CREATE TABLE staff_attendance (
    id               UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id        UUID          NOT NULL REFERENCES schools(id),
    user_id          UUID          NOT NULL REFERENCES users(id),
    attendance_date  DATE          NOT NULL,
    status           VARCHAR(12)   NOT NULL,
    remarks          VARCHAR(500),
    marked_by        UUID          REFERENCES users(id),
    created_at       TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ,
    CONSTRAINT uq_satt_user_date UNIQUE (user_id, attendance_date)
);
CREATE INDEX idx_satt_school    ON staff_attendance(school_id);
CREATE INDEX idx_satt_user_date ON staff_attendance(user_id, attendance_date);
