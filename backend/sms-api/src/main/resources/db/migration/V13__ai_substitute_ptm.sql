-- ============================================================
-- V13 — Smart Substitute & PTM Prep AI tables
-- ============================================================

-- ── 1. Substitute Assignments ────────────────────────────────
-- Tracks when a teacher is absent and who covers their classes
CREATE TABLE substitute_assignments (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id       UUID NOT NULL REFERENCES schools(id),

    absent_teacher_id   UUID NOT NULL REFERENCES users(id),
    substitute_teacher_id UUID REFERENCES users(id),
    absence_date        DATE NOT NULL,

    -- Timetable context
    period_no       SMALLINT NOT NULL,
    class_id        UUID NOT NULL REFERENCES school_classes(id),
    subject_id      UUID REFERENCES subjects(id),
    start_time      TIME NOT NULL,
    end_time        TIME NOT NULL,

    -- Status: PENDING | SUGGESTED | ASSIGNED | SELF_STUDY | CANCELLED
    status          VARCHAR(15) NOT NULL DEFAULT 'PENDING',

    -- AI suggestion metadata
    suggestion_reason   VARCHAR(500),   -- "Free period + teaches same subject"
    confidence_score    REAL,           -- 0.0 to 1.0

    assigned_by     UUID REFERENCES users(id),
    remarks         VARCHAR(500),

    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_sub_school_date ON substitute_assignments(school_id, absence_date);
CREATE INDEX idx_sub_absent      ON substitute_assignments(absent_teacher_id, absence_date);
CREATE INDEX idx_sub_substitute  ON substitute_assignments(substitute_teacher_id, absence_date);
CREATE UNIQUE INDEX uq_sub_class_period_date ON substitute_assignments(class_id, period_no, absence_date);


-- ── 2. PTM Meetings ─────────────────────────────────────────
-- Schedules a PTM event per class
CREATE TABLE ptm_meetings (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id       UUID NOT NULL REFERENCES schools(id),
    academic_year_id UUID NOT NULL REFERENCES academic_years(id),

    title           VARCHAR(200) NOT NULL,   -- "Mid-Term PTM — Class 9A"
    class_id        UUID REFERENCES school_classes(id),  -- NULL = school-wide
    meeting_date    DATE NOT NULL,
    start_time      TIME,
    end_time        TIME,

    -- Status: SCHEDULED | IN_PROGRESS | COMPLETED | CANCELLED
    status          VARCHAR(15) NOT NULL DEFAULT 'SCHEDULED',

    notes           TEXT,
    created_by      UUID NOT NULL REFERENCES users(id),

    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_ptm_school_date ON ptm_meetings(school_id, meeting_date);
CREATE INDEX idx_ptm_class       ON ptm_meetings(class_id, meeting_date);


-- ── 3. PTM Student Briefings ─────────────────────────────────
-- AI-generated per-student briefing for a PTM meeting
CREATE TABLE ptm_briefings (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ptm_meeting_id  UUID NOT NULL REFERENCES ptm_meetings(id) ON DELETE CASCADE,
    student_id      UUID NOT NULL REFERENCES students(id),
    teacher_id      UUID REFERENCES users(id),

    -- Aggregated stats (snapshot at generation time)
    attendance_pct      REAL,
    avg_marks           REAL,
    homework_completion_pct REAL,
    wellness_trend      VARCHAR(20),  -- IMPROVING | STABLE | DECLINING

    -- AI-generated content
    ai_summary          TEXT,         -- 3-4 line narrative
    talking_points      TEXT,         -- bullet points for teacher
    parent_preview      TEXT,         -- softer version for parent card

    -- Status: DRAFT | REVIEWED | SENT_TO_PARENT
    status          VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    reviewed_by     UUID REFERENCES users(id),

    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_brief_ptm      ON ptm_briefings(ptm_meeting_id);
CREATE INDEX idx_brief_student   ON ptm_briefings(student_id);
CREATE UNIQUE INDEX uq_brief_ptm_student ON ptm_briefings(ptm_meeting_id, student_id);
