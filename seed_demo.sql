-- ============================================================
--  SMS Demo Seed Data
--  Run as: psql -U sms_user -d sms_db -f seed_demo.sql
-- ============================================================

DO $$
DECLARE
  v_school_id   UUID;
  v_year_id     UUID;

  -- Teachers
  v_t1 UUID; v_t2 UUID; v_t3 UUID; v_t4 UUID; v_t5 UUID;

  -- Classes
  v_cls1 UUID; v_cls2 UUID; v_cls3 UUID; v_cls4 UUID; v_cls5 UUID;

  -- Subjects
  v_math UUID; v_eng UUID; v_sci UUID; v_sst UUID; v_hin UUID;
  v_comp UUID; v_art UUID; v_pe UUID;

  -- BCrypt hash of "Welcome@1234"
  v_pw TEXT := '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh7y';

BEGIN

  -- ── 1. Resolve school ─────────────────────────────────────────────────────
  SELECT id INTO v_school_id FROM schools LIMIT 1;

  IF v_school_id IS NULL THEN
    RAISE EXCEPTION 'No school found. Deploy and create a school first.';
  END IF;

  RAISE NOTICE 'Using school: %', v_school_id;

  -- ── 2. Academic Year ──────────────────────────────────────────────────────
  IF NOT EXISTS (SELECT 1 FROM academic_years WHERE school_id = v_school_id) THEN
    INSERT INTO academic_years (id, school_id, name, start_date, end_date, is_current, created_at)
    VALUES (gen_random_uuid(), v_school_id, '2025-2026', '2025-04-01', '2026-03-31', true, now())
    RETURNING id INTO v_year_id;
    RAISE NOTICE 'Created academic year 2025-2026';
  ELSE
    SELECT id INTO v_year_id FROM academic_years WHERE school_id = v_school_id ORDER BY created_at DESC LIMIT 1;
    RAISE NOTICE 'Academic year already exists, using: %', v_year_id;
  END IF;

  -- ── 3. Teachers (Users with role TEACHER) ─────────────────────────────────
  -- Teacher 1
  IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'priya.sharma@demo.school') THEN
    INSERT INTO users (id, school_id, email, password_hash, role, first_name, last_name, is_active, created_at, updated_at)
    VALUES (gen_random_uuid(), v_school_id, 'priya.sharma@demo.school', v_pw, 'TEACHER', 'Priya', 'Sharma', true, now(), now())
    RETURNING id INTO v_t1;
  ELSE
    SELECT id INTO v_t1 FROM users WHERE email = 'priya.sharma@demo.school';
  END IF;

  -- Teacher 2
  IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'rahul.verma@demo.school') THEN
    INSERT INTO users (id, school_id, email, password_hash, role, first_name, last_name, is_active, created_at, updated_at)
    VALUES (gen_random_uuid(), v_school_id, 'rahul.verma@demo.school', v_pw, 'TEACHER', 'Rahul', 'Verma', true, now(), now())
    RETURNING id INTO v_t2;
  ELSE
    SELECT id INTO v_t2 FROM users WHERE email = 'rahul.verma@demo.school';
  END IF;

  -- Teacher 3
  IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'sunita.nair@demo.school') THEN
    INSERT INTO users (id, school_id, email, password_hash, role, first_name, last_name, is_active, created_at, updated_at)
    VALUES (gen_random_uuid(), v_school_id, 'sunita.nair@demo.school', v_pw, 'TEACHER', 'Sunita', 'Nair', true, now(), now())
    RETURNING id INTO v_t3;
  ELSE
    SELECT id INTO v_t3 FROM users WHERE email = 'sunita.nair@demo.school';
  END IF;

  -- Teacher 4
  IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'amit.patel@demo.school') THEN
    INSERT INTO users (id, school_id, email, password_hash, role, first_name, last_name, is_active, created_at, updated_at)
    VALUES (gen_random_uuid(), v_school_id, 'amit.patel@demo.school', v_pw, 'TEACHER', 'Amit', 'Patel', true, now(), now())
    RETURNING id INTO v_t4;
  ELSE
    SELECT id INTO v_t4 FROM users WHERE email = 'amit.patel@demo.school';
  END IF;

  -- Teacher 5
  IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'kavitha.rao@demo.school') THEN
    INSERT INTO users (id, school_id, email, password_hash, role, first_name, last_name, is_active, created_at, updated_at)
    VALUES (gen_random_uuid(), v_school_id, 'kavitha.rao@demo.school', v_pw, 'TEACHER', 'Kavitha', 'Rao', true, now(), now())
    RETURNING id INTO v_t5;
  ELSE
    SELECT id INTO v_t5 FROM users WHERE email = 'kavitha.rao@demo.school';
  END IF;

  RAISE NOTICE 'Teachers ready';

  -- ── 4. Classes ────────────────────────────────────────────────────────────
  IF NOT EXISTS (SELECT 1 FROM school_classes WHERE school_id = v_school_id AND grade = 5 AND section = 'A') THEN
    INSERT INTO school_classes (id, school_id, grade, section, name, capacity, class_teacher_id, created_at)
    VALUES (gen_random_uuid(), v_school_id, 5, 'A', 'Grade 5 - A', 40, v_t1, now())
    RETURNING id INTO v_cls1;
  ELSE
    SELECT id INTO v_cls1 FROM school_classes WHERE school_id = v_school_id AND grade = 5 AND section = 'A';
  END IF;

  IF NOT EXISTS (SELECT 1 FROM school_classes WHERE school_id = v_school_id AND grade = 5 AND section = 'B') THEN
    INSERT INTO school_classes (id, school_id, grade, section, name, capacity, class_teacher_id, created_at)
    VALUES (gen_random_uuid(), v_school_id, 5, 'B', 'Grade 5 - B', 40, v_t2, now())
    RETURNING id INTO v_cls2;
  ELSE
    SELECT id INTO v_cls2 FROM school_classes WHERE school_id = v_school_id AND grade = 5 AND section = 'B';
  END IF;

  IF NOT EXISTS (SELECT 1 FROM school_classes WHERE school_id = v_school_id AND grade = 6 AND section = 'A') THEN
    INSERT INTO school_classes (id, school_id, grade, section, name, capacity, class_teacher_id, created_at)
    VALUES (gen_random_uuid(), v_school_id, 6, 'A', 'Grade 6 - A', 38, v_t3, now())
    RETURNING id INTO v_cls3;
  ELSE
    SELECT id INTO v_cls3 FROM school_classes WHERE school_id = v_school_id AND grade = 6 AND section = 'A';
  END IF;

  IF NOT EXISTS (SELECT 1 FROM school_classes WHERE school_id = v_school_id AND grade = 7 AND section = 'A') THEN
    INSERT INTO school_classes (id, school_id, grade, section, name, capacity, class_teacher_id, created_at)
    VALUES (gen_random_uuid(), v_school_id, 7, 'A', 'Grade 7 - A', 35, v_t4, now())
    RETURNING id INTO v_cls4;
  ELSE
    SELECT id INTO v_cls4 FROM school_classes WHERE school_id = v_school_id AND grade = 7 AND section = 'A';
  END IF;

  IF NOT EXISTS (SELECT 1 FROM school_classes WHERE school_id = v_school_id AND grade = 8 AND section = 'A') THEN
    INSERT INTO school_classes (id, school_id, grade, section, name, capacity, class_teacher_id, created_at)
    VALUES (gen_random_uuid(), v_school_id, 8, 'A', 'Grade 8 - A', 35, v_t5, now())
    RETURNING id INTO v_cls5;
  ELSE
    SELECT id INTO v_cls5 FROM school_classes WHERE school_id = v_school_id AND grade = 8 AND section = 'A';
  END IF;

  RAISE NOTICE 'Classes ready';

  -- ── 5. Subjects ───────────────────────────────────────────────────────────
  IF NOT EXISTS (SELECT 1 FROM subjects WHERE school_id = v_school_id AND code = 'MATH') THEN
    INSERT INTO subjects (id, school_id, name, code, type, credit_hours, created_at)
    VALUES (gen_random_uuid(), v_school_id, 'Mathematics', 'MATH', 'CORE', 5, now())
    RETURNING id INTO v_math;
  ELSE SELECT id INTO v_math FROM subjects WHERE school_id = v_school_id AND code = 'MATH'; END IF;

  IF NOT EXISTS (SELECT 1 FROM subjects WHERE school_id = v_school_id AND code = 'ENG') THEN
    INSERT INTO subjects (id, school_id, name, code, type, credit_hours, created_at)
    VALUES (gen_random_uuid(), v_school_id, 'English', 'ENG', 'CORE', 5, now())
    RETURNING id INTO v_eng;
  ELSE SELECT id INTO v_eng FROM subjects WHERE school_id = v_school_id AND code = 'ENG'; END IF;

  IF NOT EXISTS (SELECT 1 FROM subjects WHERE school_id = v_school_id AND code = 'SCI') THEN
    INSERT INTO subjects (id, school_id, name, code, type, credit_hours, created_at)
    VALUES (gen_random_uuid(), v_school_id, 'Science', 'SCI', 'CORE', 4, now())
    RETURNING id INTO v_sci;
  ELSE SELECT id INTO v_sci FROM subjects WHERE school_id = v_school_id AND code = 'SCI'; END IF;

  IF NOT EXISTS (SELECT 1 FROM subjects WHERE school_id = v_school_id AND code = 'SST') THEN
    INSERT INTO subjects (id, school_id, name, code, type, credit_hours, created_at)
    VALUES (gen_random_uuid(), v_school_id, 'Social Studies', 'SST', 'CORE', 4, now())
    RETURNING id INTO v_sst;
  ELSE SELECT id INTO v_sst FROM subjects WHERE school_id = v_school_id AND code = 'SST'; END IF;

  IF NOT EXISTS (SELECT 1 FROM subjects WHERE school_id = v_school_id AND code = 'HIN') THEN
    INSERT INTO subjects (id, school_id, name, code, type, credit_hours, created_at)
    VALUES (gen_random_uuid(), v_school_id, 'Hindi', 'HIN', 'LANGUAGE', 4, now())
    RETURNING id INTO v_hin;
  ELSE SELECT id INTO v_hin FROM subjects WHERE school_id = v_school_id AND code = 'HIN'; END IF;

  IF NOT EXISTS (SELECT 1 FROM subjects WHERE school_id = v_school_id AND code = 'COMP') THEN
    INSERT INTO subjects (id, school_id, name, code, type, credit_hours, created_at)
    VALUES (gen_random_uuid(), v_school_id, 'Computer Science', 'COMP', 'ELECTIVE', 2, now())
    RETURNING id INTO v_comp;
  ELSE SELECT id INTO v_comp FROM subjects WHERE school_id = v_school_id AND code = 'COMP'; END IF;

  IF NOT EXISTS (SELECT 1 FROM subjects WHERE school_id = v_school_id AND code = 'ART') THEN
    INSERT INTO subjects (id, school_id, name, code, type, credit_hours, created_at)
    VALUES (gen_random_uuid(), v_school_id, 'Art & Craft', 'ART', 'ACTIVITY', 1, now())
    RETURNING id INTO v_art;
  ELSE SELECT id INTO v_art FROM subjects WHERE school_id = v_school_id AND code = 'ART'; END IF;

  IF NOT EXISTS (SELECT 1 FROM subjects WHERE school_id = v_school_id AND code = 'PE') THEN
    INSERT INTO subjects (id, school_id, name, code, type, credit_hours, created_at)
    VALUES (gen_random_uuid(), v_school_id, 'Physical Education', 'PE', 'ACTIVITY', 1, now())
    RETURNING id INTO v_pe;
  ELSE SELECT id INTO v_pe FROM subjects WHERE school_id = v_school_id AND code = 'PE'; END IF;

  RAISE NOTICE 'Subjects ready';

  -- ── 6. Class-Subject-Teacher assignments ──────────────────────────────────
  -- Grade 5-A
  INSERT INTO class_subject_teachers (id, school_id, class_id, subject_id, teacher_id)
  SELECT gen_random_uuid(), v_school_id, v_cls1, sub, tea
  FROM (VALUES
    (v_math, v_t1), (v_eng, v_t2), (v_sci, v_t3),
    (v_sst, v_t4),  (v_hin, v_t5), (v_comp, v_t3),
    (v_art,  NULL), (v_pe,  NULL)
  ) AS t(sub, tea)
  WHERE NOT EXISTS (
    SELECT 1 FROM class_subject_teachers
    WHERE school_id = v_school_id AND class_id = v_cls1 AND subject_id = t.sub
  );

  -- Grade 5-B
  INSERT INTO class_subject_teachers (id, school_id, class_id, subject_id, teacher_id)
  SELECT gen_random_uuid(), v_school_id, v_cls2, sub, tea
  FROM (VALUES
    (v_math, v_t2), (v_eng, v_t1), (v_sci, v_t4),
    (v_sst, v_t5),  (v_hin, v_t3), (v_pe,  NULL)
  ) AS t(sub, tea)
  WHERE NOT EXISTS (
    SELECT 1 FROM class_subject_teachers
    WHERE school_id = v_school_id AND class_id = v_cls2 AND subject_id = t.sub
  );

  -- Grade 6-A
  INSERT INTO class_subject_teachers (id, school_id, class_id, subject_id, teacher_id)
  SELECT gen_random_uuid(), v_school_id, v_cls3, sub, tea
  FROM (VALUES
    (v_math, v_t1), (v_eng, v_t3), (v_sci, v_t2),
    (v_sst, v_t4),  (v_hin, v_t5), (v_comp, v_t3), (v_pe, NULL)
  ) AS t(sub, tea)
  WHERE NOT EXISTS (
    SELECT 1 FROM class_subject_teachers
    WHERE school_id = v_school_id AND class_id = v_cls3 AND subject_id = t.sub
  );

  -- Grade 7-A
  INSERT INTO class_subject_teachers (id, school_id, class_id, subject_id, teacher_id)
  SELECT gen_random_uuid(), v_school_id, v_cls4, sub, tea
  FROM (VALUES
    (v_math, v_t4), (v_eng, v_t2), (v_sci, v_t1),
    (v_sst, v_t3),  (v_hin, v_t5), (v_comp, v_t3), (v_pe, NULL)
  ) AS t(sub, tea)
  WHERE NOT EXISTS (
    SELECT 1 FROM class_subject_teachers
    WHERE school_id = v_school_id AND class_id = v_cls4 AND subject_id = t.sub
  );

  -- Grade 8-A
  INSERT INTO class_subject_teachers (id, school_id, class_id, subject_id, teacher_id)
  SELECT gen_random_uuid(), v_school_id, v_cls5, sub, tea
  FROM (VALUES
    (v_math, v_t5), (v_eng, v_t1), (v_sci, v_t4),
    (v_sst, v_t2),  (v_hin, v_t3), (v_comp, v_t3), (v_pe, NULL)
  ) AS t(sub, tea)
  WHERE NOT EXISTS (
    SELECT 1 FROM class_subject_teachers
    WHERE school_id = v_school_id AND class_id = v_cls5 AND subject_id = t.sub
  );

  RAISE NOTICE 'Class-Subject-Teacher assignments ready';

  -- ── 7. Students ───────────────────────────────────────────────────────────
  -- Grade 5-A students (12 students)
  INSERT INTO students (id, school_id, admission_no, roll_no, first_name, last_name,
                        date_of_birth, gender, category, is_active, admission_date,
                        class_id, academic_year_id, nationality, created_at)
  SELECT gen_random_uuid(), v_school_id, adm, roll, fn, ln, dob::date,
         gen::text, 'GEN', true, '2025-04-01'::date, v_cls1, v_year_id, 'Indian', now()
  FROM (VALUES
    ('ADM-2501','01','Aarav',   'Mehta',    '2014-03-12','MALE'),
    ('ADM-2502','02','Diya',    'Sharma',   '2014-07-05','FEMALE'),
    ('ADM-2503','03','Rohan',   'Singh',    '2014-01-20','MALE'),
    ('ADM-2504','04','Ananya',  'Patel',    '2014-09-15','FEMALE'),
    ('ADM-2505','05','Arjun',   'Nair',     '2013-11-30','MALE'),
    ('ADM-2506','06','Priya',   'Iyer',     '2014-04-22','FEMALE'),
    ('ADM-2507','07','Kabir',   'Joshi',    '2014-08-11','MALE'),
    ('ADM-2508','08','Riya',    'Gupta',    '2014-02-28','FEMALE'),
    ('ADM-2509','09','Vivaan',  'Rao',      '2013-12-05','MALE'),
    ('ADM-2510','10','Saanvi',  'Reddy',    '2014-06-17','FEMALE'),
    ('ADM-2511','11','Ishaan',  'Kumar',    '2014-05-09','MALE'),
    ('ADM-2512','12','Aisha',   'Khan',     '2014-10-03','FEMALE')
  ) AS t(adm, roll, fn, ln, dob, gen)
  WHERE NOT EXISTS (SELECT 1 FROM students WHERE school_id = v_school_id AND admission_no = t.adm);

  -- Grade 5-B students (10 students)
  INSERT INTO students (id, school_id, admission_no, roll_no, first_name, last_name,
                        date_of_birth, gender, category, is_active, admission_date,
                        class_id, academic_year_id, nationality, created_at)
  SELECT gen_random_uuid(), v_school_id, adm, roll, fn, ln, dob::date,
         gen::text, 'GEN', true, '2025-04-01'::date, v_cls2, v_year_id, 'Indian', now()
  FROM (VALUES
    ('ADM-2513','01','Nikhil',  'Bose',     '2014-02-14','MALE'),
    ('ADM-2514','02','Pooja',   'Mishra',   '2014-08-25','FEMALE'),
    ('ADM-2515','03','Aditya',  'Verma',    '2013-12-19','MALE'),
    ('ADM-2516','04','Shruti',  'Das',      '2014-03-31','FEMALE'),
    ('ADM-2517','05','Siddharth','Pillai',  '2014-07-08','MALE'),
    ('ADM-2518','06','Tanvi',   'Shah',     '2014-01-13','FEMALE'),
    ('ADM-2519','07','Harsh',   'Malhotra', '2014-09-27','MALE'),
    ('ADM-2520','08','Swara',   'Jain',     '2014-04-04','FEMALE'),
    ('ADM-2521','09','Dev',     'Pandey',   '2014-06-22','MALE'),
    ('ADM-2522','10','Kritika', 'Saxena',   '2014-11-16','FEMALE')
  ) AS t(adm, roll, fn, ln, dob, gen)
  WHERE NOT EXISTS (SELECT 1 FROM students WHERE school_id = v_school_id AND admission_no = t.adm);

  -- Grade 6-A students (10 students)
  INSERT INTO students (id, school_id, admission_no, roll_no, first_name, last_name,
                        date_of_birth, gender, category, is_active, admission_date,
                        class_id, academic_year_id, nationality, created_at)
  SELECT gen_random_uuid(), v_school_id, adm, roll, fn, ln, dob::date,
         gen::text, 'GEN', true, '2025-04-01'::date, v_cls3, v_year_id, 'Indian', now()
  FROM (VALUES
    ('ADM-2523','01','Ayaan',   'Ansari',   '2013-05-14','MALE'),
    ('ADM-2524','02','Meera',   'Pillai',   '2013-09-03','FEMALE'),
    ('ADM-2525','03','Kunal',   'Tiwari',   '2012-12-28','MALE'),
    ('ADM-2526','04','Nisha',   'Bhatt',    '2013-07-19','FEMALE'),
    ('ADM-2527','05','Raghav',  'Soni',     '2013-03-07','MALE'),
    ('ADM-2528','06','Palak',   'Agarwal',  '2013-11-23','FEMALE'),
    ('ADM-2529','07','Yash',    'Shukla',   '2013-01-31','MALE'),
    ('ADM-2530','08','Anjali',  'Dubey',    '2013-08-12','FEMALE'),
    ('ADM-2531','09','Shiven',  'Kapoor',   '2013-04-16','MALE'),
    ('ADM-2532','10','Bhavna',  'Chauhan',  '2013-10-08','FEMALE')
  ) AS t(adm, roll, fn, ln, dob, gen)
  WHERE NOT EXISTS (SELECT 1 FROM students WHERE school_id = v_school_id AND admission_no = t.adm);

  -- Grade 7-A students (8 students)
  INSERT INTO students (id, school_id, admission_no, roll_no, first_name, last_name,
                        date_of_birth, gender, category, is_active, admission_date,
                        class_id, academic_year_id, nationality, created_at)
  SELECT gen_random_uuid(), v_school_id, adm, roll, fn, ln, dob::date,
         gen::text, 'GEN', true, '2025-04-01'::date, v_cls4, v_year_id, 'Indian', now()
  FROM (VALUES
    ('ADM-2533','01','Manav',   'Khanna',   '2012-02-10','MALE'),
    ('ADM-2534','02','Divya',   'Mathur',   '2012-06-25','FEMALE'),
    ('ADM-2535','03','Arnav',   'Bajaj',    '2011-11-14','MALE'),
    ('ADM-2536','04','Simran',  'Sethi',    '2012-04-30','FEMALE'),
    ('ADM-2537','05','Parth',   'Aggarwal', '2012-08-03','MALE'),
    ('ADM-2538','06','Ishita',  'Chandra',  '2012-01-17','FEMALE'),
    ('ADM-2539','07','Vedant',  'Lal',      '2012-09-22','MALE'),
    ('ADM-2540','08','Kavya',   'Bhatia',   '2012-03-05','FEMALE')
  ) AS t(adm, roll, fn, ln, dob, gen)
  WHERE NOT EXISTS (SELECT 1 FROM students WHERE school_id = v_school_id AND admission_no = t.adm);

  -- Grade 8-A students (8 students)
  INSERT INTO students (id, school_id, admission_no, roll_no, first_name, last_name,
                        date_of_birth, gender, category, is_active, admission_date,
                        class_id, academic_year_id, nationality, created_at)
  SELECT gen_random_uuid(), v_school_id, adm, roll, fn, ln, dob::date,
         gen::text, 'GEN', true, '2025-04-01'::date, v_cls5, v_year_id, 'Indian', now()
  FROM (VALUES
    ('ADM-2541','01','Rishabh', 'Ghosh',    '2011-05-08','MALE'),
    ('ADM-2542','02','Nandini', 'Roy',      '2011-09-19','FEMALE'),
    ('ADM-2543','03','Sarthak', 'Dey',      '2010-12-31','MALE'),
    ('ADM-2544','04','Aditi',   'Sen',      '2011-07-14','FEMALE'),
    ('ADM-2545','05','Dhruv',   'Mukerjee', '2011-03-23','MALE'),
    ('ADM-2546','06','Sanya',   'Banerjee', '2011-11-07','FEMALE'),
    ('ADM-2547','07','Utkarsh', 'Chaudhary','2011-01-28','MALE'),
    ('ADM-2548','08','Mahika',  'Rastogi',  '2011-08-16','FEMALE')
  ) AS t(adm, roll, fn, ln, dob, gen)
  WHERE NOT EXISTS (SELECT 1 FROM students WHERE school_id = v_school_id AND admission_no = t.adm);

  RAISE NOTICE 'Students ready';

  RAISE NOTICE '=================================================';
  RAISE NOTICE 'Seed complete!';
  RAISE NOTICE '  5 classes | 8 subjects | 5 teachers | 48 students';
  RAISE NOTICE '  Teacher password: Welcome@1234';
  RAISE NOTICE '=================================================';

END $$;
