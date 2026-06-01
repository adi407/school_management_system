-- V4 — Fix wellness_checkins unique constraint.
-- The UNIQUE NULLS NOT DISTINCT (student_id, checkin_date) constraint incorrectly treats
-- all anonymous check-ins (student_id = NULL) as duplicates.
-- Replace with a partial unique index that only enforces uniqueness for identified students.

-- Step 1: Drop the incorrect constraint
DO $$
BEGIN
  -- Find and drop the unique constraint on wellness_checkins
  IF EXISTS (
    SELECT 1 FROM pg_constraint c
    JOIN pg_class t ON t.oid = c.conrelid
    WHERE t.relname = 'wellness_checkins' AND c.contype = 'u'
  ) THEN
    EXECUTE (
      SELECT 'ALTER TABLE wellness_checkins DROP CONSTRAINT ' || quote_ident(c.conname)
      FROM pg_constraint c
      JOIN pg_class t ON t.oid = c.conrelid
      WHERE t.relname = 'wellness_checkins' AND c.contype = 'u'
      LIMIT 1
    );
  END IF;
END;
$$;

-- Step 2: Add correct partial unique index — only one check-in per student per day
-- Anonymous check-ins (student_id IS NULL) are unrestricted.
CREATE UNIQUE INDEX IF NOT EXISTS idx_wellness_student_date_unique
    ON wellness_checkins (student_id, checkin_date)
    WHERE student_id IS NOT NULL;
