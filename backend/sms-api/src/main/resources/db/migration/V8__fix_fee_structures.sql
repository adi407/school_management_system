-- V8: Replace pre-existing fee_structures (wrong schema) with correct one

-- Drop dependent tables from the old fee schema
DROP TABLE IF EXISTS student_fees      CASCADE;
DROP TABLE IF EXISTS fee_transactions  CASCADE;

-- Remove FK from fee_payments to old fee_structures before we drop it
ALTER TABLE fee_payments DROP CONSTRAINT IF EXISTS fee_payments_fee_structure_id_fkey;

-- Drop old fee_structures (wrong columns — was created by an earlier migration)
DROP TABLE IF EXISTS fee_structures    CASCADE;

-- Drop fee_categories (replaced by inline fee_type text field)
DROP TABLE IF EXISTS fee_categories    CASCADE;

-- Recreate fee_structures with the correct schema
CREATE TABLE fee_structures (
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

CREATE INDEX idx_fee_struct_school ON fee_structures (school_id);
CREATE INDEX idx_fee_struct_class  ON fee_structures (class_id);

CREATE OR REPLACE TRIGGER set_timestamp_fee_structures
    BEFORE UPDATE ON fee_structures
    FOR EACH ROW EXECUTE FUNCTION trigger_set_timestamp();

-- Restore FK from fee_payments to the new fee_structures
ALTER TABLE fee_payments
    ADD CONSTRAINT fee_payments_fee_structure_id_fkey
    FOREIGN KEY (fee_structure_id) REFERENCES fee_structures(id);
