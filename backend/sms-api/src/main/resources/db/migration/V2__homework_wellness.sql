-- ============================================================
-- V2 — Homework Management + Campus Pulse Wellness
-- ============================================================

-- ── Homework ──────────────────────────────────────────────────
CREATE TABLE homework (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id        UUID NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    class_id         UUID NOT NULL REFERENCES school_classes(id),
    subject_id       UUID REFERENCES subjects(id),
    academic_year_id UUID REFERENCES academic_years(id),
    teacher_id       UUID NOT NULL REFERENCES users(id),
    title            VARCHAR(300) NOT NULL,
    description      TEXT NOT NULL,
    due_date         DATE NOT NULL,
    estimated_minutes INTEGER,
    attachments      JSONB DEFAULT '[]',
    is_published     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_homework_class_date ON homework(class_id, due_date DESC);
CREATE INDEX idx_homework_school     ON homework(school_id);
CREATE INDEX idx_homework_teacher    ON homework(teacher_id);
CREATE INDEX idx_homework_due        ON homework(due_date);

CREATE TABLE homework_submissions (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    homework_id  UUID NOT NULL REFERENCES homework(id) ON DELETE CASCADE,
    student_id   UUID NOT NULL REFERENCES students(id),
    school_id    UUID NOT NULL REFERENCES schools(id),
    submitted_at TIMESTAMPTZ,
    status       VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, SUBMITTED, LATE, GRADED
    content      TEXT,
    attachments  JSONB DEFAULT '[]',
    remarks      VARCHAR(500),
    grade        VARCHAR(5),
    graded_by    UUID REFERENCES users(id),
    graded_at    TIMESTAMPTZ,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(homework_id, student_id)
);
CREATE INDEX idx_hw_sub_homework ON homework_submissions(homework_id);
CREATE INDEX idx_hw_sub_student  ON homework_submissions(student_id);

-- auto update updated_at for homework
CREATE TRIGGER set_timestamp_homework
    BEFORE UPDATE ON homework
    FOR EACH ROW EXECUTE FUNCTION trigger_set_timestamp();

-- ── Campus Pulse — Anonymous Student Wellness Check-in (USP) ──
CREATE TABLE wellness_checkins (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id        UUID NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    class_id         UUID NOT NULL REFERENCES school_classes(id),
    academic_year_id UUID REFERENCES academic_years(id),
    checkin_date     DATE NOT NULL DEFAULT CURRENT_DATE,
    mood             VARCHAR(10) NOT NULL CHECK (mood IN ('GREAT','GOOD','OKAY','SAD','STRESSED')),
    -- student_id intentionally nullable to support fully anonymous mode
    student_id       UUID REFERENCES students(id),
    note             VARCHAR(200), -- optional anonymous note
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    -- one check-in per student per day (when student_id present)
    UNIQUE NULLS NOT DISTINCT (student_id, checkin_date)
);
CREATE INDEX idx_wellness_class_date  ON wellness_checkins(class_id, checkin_date);
CREATE INDEX idx_wellness_school_date ON wellness_checkins(school_id, checkin_date);
