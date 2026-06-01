-- V3 — Add missing updated_at to subjects table
-- SchoolScopedEntity.updatedAt requires this column in all extending entities.
ALTER TABLE subjects ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT now();

CREATE OR REPLACE TRIGGER set_timestamp_subjects
    BEFORE UPDATE ON subjects
    FOR EACH ROW EXECUTE FUNCTION trigger_set_timestamp();
