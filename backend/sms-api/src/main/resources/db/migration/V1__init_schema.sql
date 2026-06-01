-- ============================================================
-- V1 — Initial Schema for School Management System
-- PostgreSQL 16 with UUID PKs, JSONB, pg_trgm extension
-- ============================================================

-- Extensions
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ── Multi-Tenancy: Platforms ───────────────────────────────
CREATE TABLE platforms (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(200) NOT NULL,
    domain      VARCHAR(255) UNIQUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ── Schools ───────────────────────────────────────────────
CREATE TABLE schools (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    platform_id         UUID NOT NULL REFERENCES platforms(id),
    name                VARCHAR(200) NOT NULL,
    code                VARCHAR(20)  NOT NULL UNIQUE,
    logo_url            TEXT,
    address             TEXT,
    phone               VARCHAR(20),
    email               VARCHAR(255),
    board               VARCHAR(10)  NOT NULL DEFAULT 'CBSE',
    timezone            VARCHAR(50)  NOT NULL DEFAULT 'Asia/Kolkata',
    locale              VARCHAR(10)  NOT NULL DEFAULT 'en-IN',
    is_active           BOOLEAN      NOT NULL DEFAULT TRUE,
    subscription_tier   VARCHAR(20)  NOT NULL DEFAULT 'FREE',
    subscription_expiry DATE,
    max_students        INTEGER,
    max_staff           INTEGER,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_schools_platform ON schools(platform_id);
CREATE INDEX idx_schools_active   ON schools(is_active);
CREATE INDEX idx_schools_tier     ON schools(subscription_tier);
-- trigram index for fast name search
CREATE INDEX idx_schools_name_trgm ON schools USING gin(name gin_trgm_ops);

-- ── Feature Flags ─────────────────────────────────────────
CREATE TABLE feature_flags (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id   UUID NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    feature_key VARCHAR(60) NOT NULL,
    is_enabled  BOOLEAN NOT NULL DEFAULT FALSE,
    config      JSONB,
    enabled_by  UUID,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(school_id, feature_key)
);
CREATE INDEX idx_ff_school ON feature_flags(school_id);

-- ── Users & Auth ──────────────────────────────────────────
CREATE TABLE users (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id           UUID REFERENCES schools(id) ON DELETE CASCADE,  -- NULL for SUPER_ADMIN
    email               VARCHAR(255) NOT NULL UNIQUE,
    password_hash       VARCHAR(255) NOT NULL,
    role                VARCHAR(30)  NOT NULL,
    is_active           BOOLEAN      NOT NULL DEFAULT TRUE,
    last_login_at       TIMESTAMPTZ,
    two_factor_enabled  BOOLEAN      NOT NULL DEFAULT FALSE,
    two_factor_secret   VARCHAR(100),
    profile_photo_url   TEXT,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_users_school ON users(school_id);
CREATE INDEX idx_users_role   ON users(role);
CREATE INDEX idx_users_email_trgm ON users USING gin(email gin_trgm_ops);

CREATE TABLE refresh_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash  VARCHAR(255) NOT NULL UNIQUE,
    device_info VARCHAR(500),
    ip_address  VARCHAR(45),
    expires_at  TIMESTAMPTZ NOT NULL,
    revoked_at  TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_refresh_user  ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_token ON refresh_tokens(token_hash);

-- ── Audit Logs ────────────────────────────────────────────
CREATE TABLE audit_logs (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id   UUID,
    user_id     UUID,
    action      VARCHAR(100) NOT NULL,
    entity      VARCHAR(100) NOT NULL,
    entity_id   UUID,
    old_value   JSONB,
    new_value   JSONB,
    ip_address  VARCHAR(45),
    user_agent  TEXT,
    timestamp   TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_audit_school    ON audit_logs(school_id);
CREATE INDEX idx_audit_user      ON audit_logs(user_id);
CREATE INDEX idx_audit_entity    ON audit_logs(entity, entity_id);
CREATE INDEX idx_audit_timestamp ON audit_logs(timestamp DESC);

-- ── Academic Structure ─────────────────────────────────────
CREATE TABLE academic_years (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id   UUID NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    name        VARCHAR(20) NOT NULL,
    start_date  DATE NOT NULL,
    end_date    DATE NOT NULL,
    is_current  BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(school_id, name)
);
CREATE INDEX idx_ay_school ON academic_years(school_id);

-- Only one current academic year per school
CREATE UNIQUE INDEX idx_ay_current ON academic_years(school_id) WHERE is_current = TRUE;

CREATE TABLE terms (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    academic_year_id UUID NOT NULL REFERENCES academic_years(id) ON DELETE CASCADE,
    school_id       UUID NOT NULL REFERENCES schools(id),
    name            VARCHAR(50) NOT NULL,
    start_date      DATE NOT NULL,
    end_date        DATE NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_terms_ay ON terms(academic_year_id);

CREATE TABLE school_classes (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id       UUID NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    name            VARCHAR(50) NOT NULL,
    grade           INTEGER NOT NULL,
    section         VARCHAR(5) NOT NULL,
    capacity        INTEGER NOT NULL DEFAULT 40,
    class_teacher_id UUID REFERENCES users(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(school_id, grade, section)
);
CREATE INDEX idx_class_school ON school_classes(school_id);

CREATE TABLE subjects (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id   UUID NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    name        VARCHAR(100) NOT NULL,
    code        VARCHAR(20) NOT NULL,
    type        VARCHAR(20) NOT NULL DEFAULT 'CORE',
    credit_hours INTEGER,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(school_id, code)
);

CREATE TABLE class_subjects (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    class_id    UUID NOT NULL REFERENCES school_classes(id) ON DELETE CASCADE,
    subject_id  UUID NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    teacher_id  UUID REFERENCES users(id),
    school_id   UUID NOT NULL REFERENCES schools(id),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(class_id, subject_id)
);

-- ── Students ──────────────────────────────────────────────
CREATE TABLE students (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id           UUID NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    user_id             UUID UNIQUE REFERENCES users(id),
    admission_no        VARCHAR(50) NOT NULL,
    roll_no             VARCHAR(20),
    first_name          VARCHAR(100) NOT NULL,
    last_name           VARCHAR(100) NOT NULL,
    date_of_birth       DATE NOT NULL,
    gender              VARCHAR(10) NOT NULL,
    blood_group         VARCHAR(5),
    nationality         VARCHAR(50) DEFAULT 'Indian',
    religion            VARCHAR(50),
    caste               VARCHAR(50),
    category            VARCHAR(10) DEFAULT 'GEN',
    mother_tongue       VARCHAR(50),
    aadhaar_no          VARCHAR(12),
    class_id            UUID REFERENCES school_classes(id),
    academic_year_id    UUID REFERENCES academic_years(id),
    house_group         VARCHAR(50),
    bus_route_id        UUID,
    hostel_room_id      UUID,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    admission_date      DATE NOT NULL,
    tc_issued           BOOLEAN NOT NULL DEFAULT FALSE,
    photo_url           TEXT,
    medical_conditions  JSONB,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(school_id, admission_no)
);
CREATE INDEX idx_student_school    ON students(school_id);
CREATE INDEX idx_student_class     ON students(class_id);
CREATE INDEX idx_student_admission ON students(admission_no);
CREATE INDEX idx_student_name_trgm ON students USING gin((first_name || ' ' || last_name) gin_trgm_ops);

CREATE TABLE guardians (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id           UUID NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    school_id            UUID NOT NULL REFERENCES schools(id),
    name                 VARCHAR(200) NOT NULL,
    relation             VARCHAR(30) NOT NULL,
    phone                VARCHAR(20) NOT NULL,
    email                VARCHAR(255),
    aadhaar_no           VARCHAR(12),
    occupation           VARCHAR(100),
    address              TEXT,
    is_primary           BOOLEAN NOT NULL DEFAULT FALSE,
    is_authorized_pickup BOOLEAN NOT NULL DEFAULT FALSE,
    photo_url            TEXT,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_guardian_student ON guardians(student_id);

CREATE TABLE student_documents (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id      UUID NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    school_id       UUID NOT NULL REFERENCES schools(id),
    doc_type        VARCHAR(50) NOT NULL,
    file_name       VARCHAR(255) NOT NULL,
    file_url        TEXT NOT NULL,
    file_size       BIGINT,
    mime_type       VARCHAR(100),
    uploaded_by     UUID REFERENCES users(id),
    verified_by     UUID REFERENCES users(id),
    verified_at     TIMESTAMPTZ,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    rejection_reason TEXT,
    version         INTEGER NOT NULL DEFAULT 1,
    expires_at      DATE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_doc_student ON student_documents(student_id);

-- ── Attendance ────────────────────────────────────────────
CREATE TABLE student_attendance (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id  UUID NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    class_id    UUID NOT NULL REFERENCES school_classes(id),
    school_id   UUID NOT NULL REFERENCES schools(id),
    date        DATE NOT NULL,
    period      INTEGER,
    status      VARCHAR(20) NOT NULL DEFAULT 'PRESENT',
    marked_by   UUID REFERENCES users(id),
    marked_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    remarks     TEXT,
    UNIQUE NULLS NOT DISTINCT (student_id, date, period)
);
CREATE INDEX idx_att_student ON student_attendance(student_id);
CREATE INDEX idx_att_class_date ON student_attendance(class_id, date);
CREATE INDEX idx_att_school_date ON student_attendance(school_id, date);

-- ── Timetable ─────────────────────────────────────────────
CREATE TABLE timetables (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id       UUID NOT NULL REFERENCES schools(id),
    class_id        UUID NOT NULL REFERENCES school_classes(id),
    academic_year_id UUID NOT NULL REFERENCES academic_years(id),
    term_id         UUID REFERENCES terms(id),
    is_published    BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE timetable_slots (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    timetable_id    UUID NOT NULL REFERENCES timetables(id) ON DELETE CASCADE,
    school_id       UUID NOT NULL REFERENCES schools(id),
    day_of_week     VARCHAR(10) NOT NULL,
    period_no       INTEGER NOT NULL,
    start_time      TIME NOT NULL,
    end_time        TIME NOT NULL,
    subject_id      UUID REFERENCES subjects(id),
    teacher_id      UUID REFERENCES users(id),
    room_no         VARCHAR(20),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ── Examinations ──────────────────────────────────────────
CREATE TABLE exam_types (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id   UUID NOT NULL REFERENCES schools(id),
    name        VARCHAR(50) NOT NULL,
    weightage   NUMERIC(5,2) DEFAULT 100,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE exams (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id       UUID NOT NULL REFERENCES schools(id),
    exam_type_id    UUID REFERENCES exam_types(id),
    academic_year_id UUID REFERENCES academic_years(id),
    term_id         UUID REFERENCES terms(id),
    name            VARCHAR(200) NOT NULL,
    start_date      DATE,
    end_date        DATE,
    status          VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_exams_school ON exams(school_id);

CREATE TABLE exam_schedules (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    exam_id         UUID NOT NULL REFERENCES exams(id) ON DELETE CASCADE,
    school_id       UUID NOT NULL REFERENCES schools(id),
    class_id        UUID NOT NULL REFERENCES school_classes(id),
    subject_id      UUID NOT NULL REFERENCES subjects(id),
    date            DATE,
    start_time      TIME,
    end_time        TIME,
    room_no         VARCHAR(20),
    invigilator_id  UUID REFERENCES users(id),
    max_marks       NUMERIC(6,2) NOT NULL DEFAULT 100,
    pass_marks      NUMERIC(6,2) NOT NULL DEFAULT 35,
    duration_minutes INTEGER,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE exam_results (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    exam_schedule_id UUID NOT NULL REFERENCES exam_schedules(id),
    student_id      UUID NOT NULL REFERENCES students(id),
    school_id       UUID NOT NULL REFERENCES schools(id),
    marks_obtained  NUMERIC(6,2),
    grade           VARCHAR(5),
    is_absent       BOOLEAN NOT NULL DEFAULT FALSE,
    is_exempt       BOOLEAN NOT NULL DEFAULT FALSE,
    remarks         TEXT,
    entered_by      UUID REFERENCES users(id),
    verified_by     UUID REFERENCES users(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(exam_schedule_id, student_id)
);
CREATE INDEX idx_results_student ON exam_results(student_id);

-- ── Fee Management ────────────────────────────────────────
CREATE TABLE fee_categories (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id   UUID NOT NULL REFERENCES schools(id),
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    is_mandatory BOOLEAN NOT NULL DEFAULT TRUE,
    frequency   VARCHAR(20) NOT NULL DEFAULT 'ANNUAL',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE fee_structures (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id       UUID NOT NULL REFERENCES schools(id),
    fee_category_id UUID NOT NULL REFERENCES fee_categories(id),
    class_id        UUID NOT NULL REFERENCES school_classes(id),
    academic_year_id UUID NOT NULL REFERENCES academic_years(id),
    amount          NUMERIC(12,2) NOT NULL,
    due_date        DATE,
    late_fine_per_day NUMERIC(8,2) DEFAULT 0,
    student_category VARCHAR(10),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE student_fees (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id      UUID NOT NULL REFERENCES students(id),
    fee_structure_id UUID NOT NULL REFERENCES fee_structures(id),
    school_id       UUID NOT NULL REFERENCES schools(id),
    academic_year_id UUID NOT NULL REFERENCES academic_years(id),
    due_amount      NUMERIC(12,2) NOT NULL,
    discount_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
    net_amount      NUMERIC(12,2) NOT NULL,
    paid_amount     NUMERIC(12,2) NOT NULL DEFAULT 0,
    balance_amount  NUMERIC(12,2) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_sfee_student ON student_fees(student_id);
CREATE INDEX idx_sfee_school  ON student_fees(school_id);

CREATE TABLE fee_transactions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_fee_id  UUID NOT NULL REFERENCES student_fees(id),
    school_id       UUID NOT NULL REFERENCES schools(id),
    amount          NUMERIC(12,2) NOT NULL,
    payment_mode    VARCHAR(30) NOT NULL,
    transaction_ref VARCHAR(100),
    gateway         VARCHAR(30),
    gateway_ref     VARCHAR(200),
    receipt_no      VARCHAR(50),
    paid_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    collected_by    UUID REFERENCES users(id),
    s3_receipt_key  TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_txn_student_fee ON fee_transactions(student_fee_id);
CREATE INDEX idx_txn_school      ON fee_transactions(school_id);

-- ── Staff & HR ────────────────────────────────────────────
CREATE TABLE staff (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id       UUID NOT NULL REFERENCES schools(id),
    user_id         UUID UNIQUE REFERENCES users(id),
    employee_id     VARCHAR(50) NOT NULL,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    designation     VARCHAR(100),
    department      VARCHAR(100),
    qualification   VARCHAR(200),
    experience      INTEGER,
    joining_date    DATE,
    employment_type VARCHAR(30),
    phone           VARCHAR(20),
    aadhaar_no      VARCHAR(12),
    pan_no          VARCHAR(10),
    bank_account_no VARCHAR(30),
    bank_ifsc       VARCHAR(15),
    pf_no           VARCHAR(30),
    esi_no          VARCHAR(30),
    basic_salary    NUMERIC(12,2),
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    photo_url       TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(school_id, employee_id)
);
CREATE INDEX idx_staff_school ON staff(school_id);

CREATE TABLE staff_leaves (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    staff_id    UUID NOT NULL REFERENCES staff(id),
    school_id   UUID NOT NULL REFERENCES schools(id),
    leave_type  VARCHAR(5) NOT NULL,
    from_date   DATE NOT NULL,
    to_date     DATE NOT NULL,
    total_days  NUMERIC(4,1) NOT NULL,
    reason      TEXT,
    status      VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    approved_by UUID REFERENCES users(id),
    applied_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    action_at   TIMESTAMPTZ
);

-- ── Library ───────────────────────────────────────────────
CREATE TABLE book_categories (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id   UUID NOT NULL REFERENCES schools(id),
    name        VARCHAR(100) NOT NULL,
    dewey_code  VARCHAR(20),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE books (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id       UUID NOT NULL REFERENCES schools(id),
    category_id     UUID REFERENCES book_categories(id),
    title           VARCHAR(300) NOT NULL,
    author          VARCHAR(200),
    isbn            VARCHAR(20),
    publisher       VARCHAR(200),
    edition         VARCHAR(50),
    language        VARCHAR(30) DEFAULT 'English',
    total_copies    INTEGER NOT NULL DEFAULT 1,
    available_copies INTEGER NOT NULL DEFAULT 1,
    shelf_location  VARCHAR(50),
    cover_url       TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_books_school ON books(school_id);
CREATE INDEX idx_books_title_trgm ON books USING gin(title gin_trgm_ops);

CREATE TABLE book_issues (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    book_id         UUID NOT NULL REFERENCES books(id),
    school_id       UUID NOT NULL REFERENCES schools(id),
    issued_to_user  UUID NOT NULL REFERENCES users(id),
    issue_date      DATE NOT NULL,
    due_date        DATE NOT NULL,
    return_date     DATE,
    fine_amount     NUMERIC(8,2) DEFAULT 0,
    fine_paid       BOOLEAN NOT NULL DEFAULT FALSE,
    status          VARCHAR(20) NOT NULL DEFAULT 'ISSUED',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ── Transport ─────────────────────────────────────────────
CREATE TABLE routes (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id           UUID NOT NULL REFERENCES schools(id),
    name                VARCHAR(100) NOT NULL,
    driver_name         VARCHAR(100),
    driver_phone        VARCHAR(20),
    driver_license      VARCHAR(30),
    vehicle_no          VARCHAR(20),
    vehicle_capacity    INTEGER,
    fitness_cert_expiry DATE,
    insurance_expiry    DATE,
    gps_device_id       VARCHAR(50),
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE route_stops (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    route_id        UUID NOT NULL REFERENCES routes(id) ON DELETE CASCADE,
    school_id       UUID NOT NULL REFERENCES schools(id),
    stop_name       VARCHAR(100) NOT NULL,
    latitude        NUMERIC(10,7),
    longitude       NUMERIC(10,7),
    arrival_time    TIME,
    departure_time  TIME,
    order_no        INTEGER NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ── Communication ─────────────────────────────────────────
CREATE TABLE announcements (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id       UUID NOT NULL REFERENCES schools(id),
    title           VARCHAR(300) NOT NULL,
    content         TEXT NOT NULL,
    target_roles    JSONB DEFAULT '[]',
    target_class_ids JSONB DEFAULT '[]',
    is_pinned       BOOLEAN NOT NULL DEFAULT FALSE,
    publish_at      TIMESTAMPTZ,
    expires_at      TIMESTAMPTZ,
    author_id       UUID NOT NULL REFERENCES users(id),
    attachments     JSONB DEFAULT '[]',
    read_count      INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_ann_school ON announcements(school_id);

CREATE TABLE notifications (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id),
    school_id       UUID,
    type            VARCHAR(50) NOT NULL,
    title           VARCHAR(300) NOT NULL,
    body            TEXT,
    data            JSONB,
    channel         VARCHAR(20) NOT NULL DEFAULT 'IN_APP',
    is_read         BOOLEAN NOT NULL DEFAULT FALSE,
    sent_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    delivery_status VARCHAR(20) NOT NULL DEFAULT 'SENT'
);
CREATE INDEX idx_notif_user ON notifications(user_id, is_read);

CREATE TABLE events (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id       UUID NOT NULL REFERENCES schools(id),
    title           VARCHAR(300) NOT NULL,
    description     TEXT,
    event_type      VARCHAR(50),
    start_date      TIMESTAMPTZ NOT NULL,
    end_date        TIMESTAMPTZ,
    venue           VARCHAR(200),
    rsvp_required   BOOLEAN NOT NULL DEFAULT FALSE,
    max_attendees   INTEGER,
    created_by      UUID REFERENCES users(id),
    status          VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE grievances (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id       UUID NOT NULL REFERENCES schools(id),
    submitted_by    UUID NOT NULL REFERENCES users(id),
    category        VARCHAR(50) NOT NULL,
    subject         VARCHAR(300) NOT NULL,
    description     TEXT NOT NULL,
    attachments     JSONB DEFAULT '[]',
    status          VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    priority        VARCHAR(10) NOT NULL DEFAULT 'MEDIUM',
    assigned_to     UUID REFERENCES users(id),
    resolution      TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    resolved_at     TIMESTAMPTZ
);

-- ── System Config ─────────────────────────────────────────
CREATE TABLE school_settings (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id   UUID NOT NULL REFERENCES schools(id),
    key         VARCHAR(100) NOT NULL,
    value       JSONB NOT NULL,
    category    VARCHAR(50),
    updated_by  UUID REFERENCES users(id),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(school_id, key)
);

CREATE TABLE holiday_calendars (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id       UUID NOT NULL REFERENCES schools(id),
    academic_year_id UUID REFERENCES academic_years(id),
    date            DATE NOT NULL,
    name            VARCHAR(200) NOT NULL,
    type            VARCHAR(30) NOT NULL,
    description     TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ── Extra-Curricular ──────────────────────────────────────
CREATE TABLE activities (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id       UUID NOT NULL REFERENCES schools(id),
    name            VARCHAR(200) NOT NULL,
    category        VARCHAR(50) NOT NULL,
    description     TEXT,
    coordinator_id  UUID REFERENCES users(id),
    schedule        TEXT,
    venue           VARCHAR(200),
    max_strength    INTEGER,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    academic_year_id UUID REFERENCES academic_years(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE activity_enrollments (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    activity_id UUID NOT NULL REFERENCES activities(id),
    student_id  UUID NOT NULL REFERENCES students(id),
    school_id   UUID NOT NULL REFERENCES schools(id),
    enrolled_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    enrolled_by UUID REFERENCES users(id),
    status      VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    UNIQUE(activity_id, student_id)
);

CREATE TABLE achievements (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id      UUID NOT NULL REFERENCES students(id),
    school_id       UUID NOT NULL REFERENCES schools(id),
    title           VARCHAR(300) NOT NULL,
    category        VARCHAR(50) NOT NULL,
    level           VARCHAR(30) NOT NULL,
    date            DATE NOT NULL,
    description     TEXT,
    certificate_s3_key TEXT,
    added_by        UUID REFERENCES users(id),
    is_verified     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_achievements_student ON achievements(student_id);

-- ── Hostel ────────────────────────────────────────────────
CREATE TABLE hostels (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id   UUID NOT NULL REFERENCES schools(id),
    name        VARCHAR(200) NOT NULL,
    type        VARCHAR(20) NOT NULL DEFAULT 'BOYS',
    warden_id   UUID REFERENCES users(id),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE hostel_rooms (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hostel_id   UUID NOT NULL REFERENCES hostels(id),
    school_id   UUID NOT NULL REFERENCES schools(id),
    room_no     VARCHAR(20) NOT NULL,
    floor       INTEGER,
    type        VARCHAR(30),
    capacity    INTEGER NOT NULL DEFAULT 2,
    occupancy   INTEGER NOT NULL DEFAULT 0,
    monthly_fee NUMERIC(10,2),
    facilities  JSONB DEFAULT '{}',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(hostel_id, room_no)
);

-- ── Password Reset Tokens ─────────────────────────────────
CREATE TABLE password_reset_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash  VARCHAR(255) NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ NOT NULL,
    used_at     TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ── Triggers: auto update updated_at ─────────────────────
CREATE OR REPLACE FUNCTION trigger_set_timestamp()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DO $$
DECLARE
    tbl TEXT;
BEGIN
    FOR tbl IN SELECT unnest(ARRAY[
        'schools','users','academic_years','school_classes',
        'students','student_fees','exams','staff','timetables','announcements'
    ]) LOOP
        EXECUTE format('
            CREATE TRIGGER set_timestamp_%I
            BEFORE UPDATE ON %I
            FOR EACH ROW EXECUTE FUNCTION trigger_set_timestamp();', tbl, tbl);
    END LOOP;
END;
$$;
