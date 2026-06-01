-- V7: Fee structures and payment tracking

CREATE TABLE IF NOT EXISTS fee_structures (
    id               UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id        UUID           NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    class_id         UUID           REFERENCES school_classes(id),
    academic_year_id UUID           REFERENCES academic_years(id),
    fee_type         VARCHAR(100)   NOT NULL,
    amount           NUMERIC(10,2)  NOT NULL,
    due_date         DATE,
    is_recurring     BOOLEAN        NOT NULL DEFAULT false,
    frequency        VARCHAR(10)    CHECK (frequency IN ('MONTHLY','QUARTERLY','ANNUAL','ONE_TIME')),
    description      VARCHAR(500),
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS fee_payments (
    id                UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id         UUID           NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    student_id        UUID           NOT NULL REFERENCES students(id),
    fee_structure_id  UUID           REFERENCES fee_structures(id),
    collected_by      UUID           NOT NULL REFERENCES users(id),
    amount_paid       NUMERIC(10,2)  NOT NULL,
    payment_date      DATE           NOT NULL,
    payment_mode      VARCHAR(20)    NOT NULL,
    receipt_no        VARCHAR(30)    NOT NULL UNIQUE,
    remarks           VARCHAR(500),
    created_at        TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_fee_struct_school ON fee_structures (school_id);
CREATE INDEX IF NOT EXISTS idx_fee_struct_class  ON fee_structures (class_id);
CREATE INDEX IF NOT EXISTS idx_fee_pay_student   ON fee_payments (student_id);
CREATE INDEX IF NOT EXISTS idx_fee_pay_school    ON fee_payments (school_id);

CREATE OR REPLACE TRIGGER set_timestamp_fee_structures
    BEFORE UPDATE ON fee_structures
    FOR EACH ROW EXECUTE FUNCTION trigger_set_timestamp();

CREATE OR REPLACE TRIGGER set_timestamp_fee_payments
    BEFORE UPDATE ON fee_payments
    FOR EACH ROW EXECUTE FUNCTION trigger_set_timestamp();
