-- V6: Attendance tracking

CREATE TABLE IF NOT EXISTS attendance (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id       UUID        NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    student_id      UUID        NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    class_id        UUID        NOT NULL REFERENCES school_classes(id),
    marked_by       UUID        NOT NULL REFERENCES users(id),
    attendance_date DATE        NOT NULL,
    status          VARCHAR(10) NOT NULL CHECK (status IN ('PRESENT','ABSENT','LATE','HOLIDAY','EXCUSED')),
    remarks         VARCHAR(500),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_att_student_date UNIQUE (student_id, attendance_date)
);

CREATE INDEX IF NOT EXISTS idx_att_class_date   ON attendance (class_id, attendance_date);
CREATE INDEX IF NOT EXISTS idx_att_student_date ON attendance (student_id, attendance_date);
CREATE INDEX IF NOT EXISTS idx_att_school       ON attendance (school_id);

CREATE OR REPLACE TRIGGER set_timestamp_attendance
    BEFORE UPDATE ON attendance
    FOR EACH ROW EXECUTE FUNCTION trigger_set_timestamp();
