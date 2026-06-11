-- ============================================================================
-- V11: Create tables that exist as JPA entities but were never migrated
-- ============================================================================

-- ── 1. class_subject_teachers ─────────────────────────────────────────────
CREATE TABLE class_subject_teachers (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id   UUID         NOT NULL REFERENCES schools(id),
    class_id    UUID         NOT NULL REFERENCES school_classes(id),
    subject_id  UUID         NOT NULL REFERENCES subjects(id),
    teacher_id  UUID                  REFERENCES users(id),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ,
    UNIQUE (school_id, class_id, subject_id)
);
CREATE INDEX idx_cst_school  ON class_subject_teachers(school_id);
CREATE INDEX idx_cst_class   ON class_subject_teachers(class_id);
CREATE INDEX idx_cst_teacher ON class_subject_teachers(teacher_id);

-- ── 2. staff_module_assignments ───────────────────────────────────────────
CREATE TABLE staff_module_assignments (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id        UUID         NOT NULL REFERENCES schools(id),
    user_id          UUID         NOT NULL REFERENCES users(id),
    module           VARCHAR(30)  NOT NULL,
    sub_permissions  JSONB,
    assigned_by      UUID                  REFERENCES users(id),
    assigned_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    is_active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ,
    CONSTRAINT uq_sma_user_module UNIQUE (school_id, user_id, module)
);
CREATE INDEX idx_sma_school      ON staff_module_assignments(school_id);
CREATE INDEX idx_sma_user        ON staff_module_assignments(user_id);
CREATE INDEX idx_sma_school_user ON staff_module_assignments(school_id, user_id);

-- ── 3. expense_entries ────────────────────────────────────────────────────
CREATE TABLE expense_entries (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id       UUID           NOT NULL REFERENCES schools(id),
    category        VARCHAR(30)    NOT NULL,
    description     VARCHAR(200)   NOT NULL,
    amount          NUMERIC(12,2)  NOT NULL,
    expense_date    DATE           NOT NULL,
    reference_no    VARCHAR(100),
    attachment_url  VARCHAR(500),
    entered_by      UUID                    REFERENCES users(id),
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ
);
CREATE INDEX idx_expense_school   ON expense_entries(school_id);
CREATE INDEX idx_expense_date     ON expense_entries(school_id, expense_date);
CREATE INDEX idx_expense_category ON expense_entries(school_id, category);
