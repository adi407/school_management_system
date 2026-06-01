-- ── Exams ─────────────────────────────────────────────────────────────────────
DROP TABLE IF EXISTS exam_results CASCADE;
DROP TABLE IF EXISTS exam_schedules CASCADE;
DROP TABLE IF EXISTS exams CASCADE;

CREATE TABLE exams (
    id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id        UUID         NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    academic_year_id UUID         REFERENCES academic_years(id),
    class_id         UUID         REFERENCES school_classes(id),   -- NULL = all classes
    name             VARCHAR(200) NOT NULL,
    exam_type        VARCHAR(50)  NOT NULL,  -- UNIT_TEST | MIDTERM | PREBOARD | ANNUAL
    start_date       DATE         NOT NULL,
    end_date         DATE         NOT NULL,
    total_subjects   INT          NOT NULL DEFAULT 0,
    description      TEXT,
    status           VARCHAR(20)  NOT NULL DEFAULT 'UPCOMING', -- UPCOMING | ONGOING | COMPLETED
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_exam_school     ON exams(school_id);
CREATE INDEX idx_exam_class      ON exams(class_id);

GRANT ALL PRIVILEGES ON TABLE exams TO sms_user;

-- ── Library ───────────────────────────────────────────────────────────────────
DROP TABLE IF EXISTS book_issues CASCADE;
DROP TABLE IF EXISTS books CASCADE;

CREATE TABLE books (
    id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id        UUID         NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    title            VARCHAR(300) NOT NULL,
    author           VARCHAR(200),
    isbn             VARCHAR(30),
    category         VARCHAR(100),
    total_copies     INT          NOT NULL DEFAULT 1,
    available_copies INT          NOT NULL DEFAULT 1,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_book_school ON books(school_id);

CREATE TABLE book_issues (
    id             UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id      UUID         NOT NULL,
    book_id        UUID         NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    student_id     UUID         REFERENCES students(id),
    borrower_name  VARCHAR(200),
    issue_date     DATE         NOT NULL,
    due_date       DATE         NOT NULL,
    return_date    DATE,
    is_returned    BOOLEAN      NOT NULL DEFAULT false,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_book_issue_school ON book_issues(school_id);
CREATE INDEX idx_book_issue_book   ON book_issues(book_id);

GRANT ALL PRIVILEGES ON TABLE books       TO sms_user;
GRANT ALL PRIVILEGES ON TABLE book_issues TO sms_user;

-- ── Activities ────────────────────────────────────────────────────────────────
DROP TABLE IF EXISTS activity_enrollments CASCADE;
DROP TABLE IF EXISTS activities CASCADE;

CREATE TABLE activities (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id   UUID         NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    name        VARCHAR(200) NOT NULL,
    category    VARCHAR(50)  NOT NULL,  -- SPORTS | ACADEMIC | MUSIC | ARTS | CULTURAL
    coach       VARCHAR(200),
    schedule    VARCHAR(200),
    capacity    INT          NOT NULL DEFAULT 30,
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_activity_school ON activities(school_id);

GRANT ALL PRIVILEGES ON TABLE activities TO sms_user;
