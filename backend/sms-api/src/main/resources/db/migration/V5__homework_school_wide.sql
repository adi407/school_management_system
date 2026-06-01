-- V5: Allow school-wide homework (no class required)
--     class_id becomes nullable so a post can target all classes.

-- 1. Drop NOT NULL on class_id
ALTER TABLE homework ALTER COLUMN class_id DROP NOT NULL;

-- 2. Add is_school_wide flag  (true = visible to all classes in the school)
ALTER TABLE homework ADD COLUMN IF NOT EXISTS is_school_wide BOOLEAN NOT NULL DEFAULT false;

-- 3. Replace the old class+date index with a partial one that only covers class-scoped rows
DROP INDEX IF EXISTS idx_homework_class_date;
CREATE INDEX idx_homework_class_date
    ON homework (class_id, due_date DESC)
    WHERE class_id IS NOT NULL;

-- 4. Separate index for school-wide posts
CREATE INDEX idx_homework_school_wide
    ON homework (school_id, due_date DESC)
    WHERE is_school_wide = true;
