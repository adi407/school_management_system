-- ============================================================
-- V14: Seed demo data — platform, schools, demo-login users
-- Uses ON CONFLICT DO NOTHING so it's safe to re-run on
-- databases that already have the data (e.g. local dev).
-- ============================================================

-- ── Platform ────────────────────────────────────────────────
INSERT INTO platforms (id, name, domain, created_at)
VALUES ('735487a6-4e5e-4ebb-a32f-360dde6f91f7', 'EduCloud Platform', 'educloud.in', NOW())
ON CONFLICT (id) DO NOTHING;

-- ── Schools ─────────────────────────────────────────────────
INSERT INTO schools (id, platform_id, name, code, email, address, board, timezone, locale, subscription_tier, is_active, created_at, updated_at)
VALUES
  ('82e3b66f-35ec-4b6e-a5c5-a96188eabe83', '735487a6-4e5e-4ebb-a32f-360dde6f91f7', 'Gita Bal Niketan',          'GBN', 'info@gitabn.edu.in',       'Sector 21D, Faridabad, Haryana 121001', 'CBSE', 'Asia/Kolkata', 'en-IN', 'PREMIUM',     true, NOW(), NOW()),
  ('785a10c6-3507-4d08-a649-57cea55e5fec', '735487a6-4e5e-4ebb-a32f-360dde6f91f7', 'Greenfield Academy',        'GFA', 'admin@greenfield.edu.in',  '',                                      'CBSE', 'Asia/Kolkata', 'en-IN', 'PREMIUM',     true, NOW(), NOW()),
  ('3198e93c-7885-42f5-97bd-3bef603e92b9', '735487a6-4e5e-4ebb-a32f-360dde6f91f7', 'Future Tech International',  'FTI', 'admin@futuretech.edu.in',  '',                                      'IB',   'Asia/Kolkata', 'en-IN', 'ENTERPRISE',  true, NOW(), NOW()),
  ('d15c16e5-6ed6-4f77-8349-9944ff37755a', '735487a6-4e5e-4ebb-a32f-360dde6f91f7', 'Sunrise Public School',      'SPS', 'admin@sunrise.edu.in',     '',                                      'ICSE', 'Asia/Kolkata', 'en-IN', 'BASIC',       true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- ── Super Admin (no school) ─────────────────────────────────
-- Password: Admin@1234
INSERT INTO users (id, email, password_hash, role, is_active, two_factor_enabled, created_at, updated_at)
VALUES
  ('377ba4f0-07fa-46a9-aef0-498873730322',
   'superadmin@educloud.in',
   '$2a$12$Zvmkpo7dZ2TcMSXJBCGiLu5zHzXdbEoQSmK6QFQu3UfmBBEBhWAii',
   'SUPER_ADMIN', true, false, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- ── School Admin — Gita Bal Niketan ─────────────────────────
-- Password: Admin@1234
INSERT INTO users (id, school_id, email, password_hash, role, is_active, two_factor_enabled, created_at, updated_at)
VALUES
  ('3b50410d-3f04-4af0-916a-0089e517dc20',
   '82e3b66f-35ec-4b6e-a5c5-a96188eabe83',
   'admin@gitabn.edu.in',
   '$2a$12$6L4YpUuQCO5hd1ZWomjz8Oe0FijACnM.XVw1YS3CXAQzgvpB9RqKm',
   'SCHOOL_ADMIN', true, false, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- ── Teacher — Amit Sharma ───────────────────────────────────
-- Password: Teacher@2025
INSERT INTO users (id, school_id, email, password_hash, role, first_name, last_name, is_active, two_factor_enabled, created_at, updated_at)
VALUES
  ('65cb44f9-0997-458f-a63f-8380b2faea69',
   '82e3b66f-35ec-4b6e-a5c5-a96188eabe83',
   'amit.sharma@gitabn.edu.in',
   '$2a$12$fuv4nDzd4C31Zd64/cW8P.DQysopAcDfC3v.1IEpWucbwmaSl3tMG',
   'TEACHER', 'Amit', 'Sharma', true, false, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- ── Accountant — Rajesh Taneja ──────────────────────────────
-- Password: Staff@2025
INSERT INTO users (id, school_id, email, password_hash, role, first_name, last_name, is_active, two_factor_enabled, created_at, updated_at)
VALUES
  ('6772dfa9-8c8e-4124-8346-ec4ed4394fc2',
   '82e3b66f-35ec-4b6e-a5c5-a96188eabe83',
   'accountant@gitabn.edu.in',
   '$2a$12$xOY3XIsvdWSoySk/uNOV2u36omLZ4RfTtBGslr/CEHWb439zstBGK',
   'ACCOUNTANT', 'Rajesh', 'Taneja', true, false, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- ── Parent — Akash Arora ────────────────────────────────────
-- Password: Parent@2025
INSERT INTO users (id, school_id, email, password_hash, role, first_name, last_name, is_active, two_factor_enabled, created_at, updated_at)
VALUES
  ('91addc03-095d-4f65-ae41-d4aaa8c90f18',
   '82e3b66f-35ec-4b6e-a5c5-a96188eabe83',
   'akash.arora@gitabn.edu.in',
   '$2b$12$zwuKA1guMIC9jeZ4mvcKn.8eFjQGskIeebcBLeAGUX9OMX8VERPhC',
   'PARENT', 'Akash', 'Arora', true, false, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- ── School Admins for other schools (so super-admin view works) ─
INSERT INTO users (id, school_id, email, password_hash, role, is_active, two_factor_enabled, created_at, updated_at)
VALUES
  ('e69bc92f-03bb-4caa-abcd-fdb7ca39a0ba', '785a10c6-3507-4d08-a649-57cea55e5fec', 'admin@greenfield.edu.in',  '$2a$12$s0U6qxXyuEbZ4i6v1fY2duNp5hGXFjDnBFKpSI0wUvybUqJ0afmn.', 'SCHOOL_ADMIN', true, false, NOW(), NOW()),
  ('acebea55-a84d-48eb-96b1-0b0c019a5e97', '3198e93c-7885-42f5-97bd-3bef603e92b9', 'admin@futuretech.edu.in',  '$2a$12$YX91jtijokTepJ1vAxCKZuB742GVIq4OvJ4lJBCyERFgqyyCtBwgy', 'SCHOOL_ADMIN', true, false, NOW(), NOW()),
  ('41eef269-8500-4d2b-8bc4-3e6b6ff92714', 'd15c16e5-6ed6-4f77-8349-9944ff37755a', 'admin@sunrise.edu.in',     '$2a$12$OyPawaenpqH1eAZ4tW.6FuBDSuZ1/LSzcPHDt1plag3AHw/Jbc7Ry', 'SCHOOL_ADMIN', true, false, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;
