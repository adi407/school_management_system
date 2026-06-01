-- V9: User profile fields + Announcements (fresh) + Timetable (fresh)

-- ── User profile fields ────────────────────────────────────────────────────
ALTER TABLE users ADD COLUMN IF NOT EXISTS first_name  VARCHAR(80);
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_name   VARCHAR(80);
ALTER TABLE users ADD COLUMN IF NOT EXISTS phone       VARCHAR(20);
ALTER TABLE users ADD COLUMN IF NOT EXISTS department  VARCHAR(100);

-- ── Announcements — drop old schema, recreate with our design ─────────────
DROP TABLE IF EXISTS announcements CASCADE;

CREATE TABLE announcements (
    id             UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id      UUID         NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    title          VARCHAR(200) NOT NULL,
    body           TEXT         NOT NULL,
    target_roles   TEXT[]       NOT NULL DEFAULT '{}',
    published_by   UUID         NOT NULL REFERENCES users(id),
    published_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    expires_at     TIMESTAMPTZ,
    is_pinned      BOOLEAN      NOT NULL DEFAULT false,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_ann_school ON announcements (school_id, published_at DESC);

CREATE OR REPLACE TRIGGER set_timestamp_announcements
    BEFORE UPDATE ON announcements
    FOR EACH ROW EXECUTE FUNCTION trigger_set_timestamp();

-- ── Timetable — drop old schema (timetable_slots depends on timetables) ───
DROP TABLE IF EXISTS timetable_slots CASCADE;
DROP TABLE IF EXISTS timetables        CASCADE;

CREATE TABLE timetable_slots (
    id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id        UUID         NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    class_id         UUID         NOT NULL REFERENCES school_classes(id) ON DELETE CASCADE,
    academic_year_id UUID         NOT NULL REFERENCES academic_years(id),
    day_of_week      VARCHAR(3)   NOT NULL CHECK (day_of_week IN ('MON','TUE','WED','THU','FRI','SAT')),
    period_no        SMALLINT     NOT NULL CHECK (period_no BETWEEN 1 AND 12),
    subject_id       UUID         REFERENCES subjects(id),
    teacher_id       UUID         REFERENCES users(id),
    start_time       TIME         NOT NULL,
    end_time         TIME         NOT NULL,
    room_no          VARCHAR(20),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_slot_class_day_period UNIQUE (class_id, day_of_week, period_no)
);

CREATE INDEX idx_tt_school  ON timetable_slots (school_id);
CREATE INDEX idx_tt_class   ON timetable_slots (class_id);
CREATE INDEX idx_tt_teacher ON timetable_slots (teacher_id);

CREATE OR REPLACE TRIGGER set_timestamp_timetable_slots
    BEFORE UPDATE ON timetable_slots
    FOR EACH ROW EXECUTE FUNCTION trigger_set_timestamp();
