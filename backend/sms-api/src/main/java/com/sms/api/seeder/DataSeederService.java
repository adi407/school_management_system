package com.sms.api.seeder;

import com.sms.api.entity.*;
import com.sms.api.repository.*;
import com.sms.core.enums.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Seeds development data on startup.
 * Only runs with the 'dev' Spring profile (spring.profiles.active=dev).
 * Idempotent — skips seeding if platform data already exists.
 */
@Component
@Profile({"dev","default"})
public class DataSeederService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeederService.class);

    private final PlatformRepository platformRepository;
    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;
    private final FeatureFlagRepository featureFlagRepository;
    private final AcademicYearRepository academicYearRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final SubjectRepository subjectRepository;
    private final StudentRepository studentRepository;
    private final GuardianRepository guardianRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeederService(
        PlatformRepository platformRepository,
        SchoolRepository schoolRepository,
        UserRepository userRepository,
        FeatureFlagRepository featureFlagRepository,
        AcademicYearRepository academicYearRepository,
        SchoolClassRepository schoolClassRepository,
        SubjectRepository subjectRepository,
        StudentRepository studentRepository,
        GuardianRepository guardianRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.platformRepository    = platformRepository;
        this.schoolRepository      = schoolRepository;
        this.userRepository        = userRepository;
        this.featureFlagRepository = featureFlagRepository;
        this.academicYearRepository = academicYearRepository;
        this.schoolClassRepository = schoolClassRepository;
        this.subjectRepository = subjectRepository;
        this.studentRepository = studentRepository;
        this.guardianRepository = guardianRepository;
        this.passwordEncoder       = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Seeding development data (idempotent)...");

        // ── Super Admin ────────────────────────────────────────
        // Ensure the known dev credentials always work even if seed already ran previously.
        User superAdmin = userRepository.findByEmail("superadmin@educloud.in")
            .orElseGet(User::new);
        superAdmin.setEmail("superadmin@educloud.in");
        superAdmin.setPasswordHash(passwordEncoder.encode("Admin@1234"));
        superAdmin.setRole(Role.SUPER_ADMIN);
        superAdmin = userRepository.save(superAdmin);

        // ── Schools ────────────────────────────────────────────
        seedSchool("Greenfield Academy",    "GFA", BoardType.CBSE,  SubscriptionTier.PREMIUM,    "admin@greenfield.edu.in");
        seedSchool("Sunrise Public School", "SPS", BoardType.ICSE,  SubscriptionTier.BASIC,      "admin@sunrise.edu.in");
        seedSchool("Future Tech International", "FTI", BoardType.IB, SubscriptionTier.ENTERPRISE, "admin@futuretech.edu.in");

        log.info("Seed data complete. Login: superadmin@educloud.in / Admin@1234");
    }

    private void seedSchool(String name, String code, BoardType board, SubscriptionTier tier, String adminEmail) {
        Platform platform = getOrCreatePlatform();
        School school = schoolRepository.findByCode(code).orElseGet(() -> {
            School created = new School();
            created.setPlatform(platform);
            created.setName(name);
            created.setCode(code);
            created.setBoard(board);
            created.setSubscriptionTier(tier);
            created.setEmail(adminEmail);
            created.setSubscriptionExpiry(LocalDate.now().plusYears(1));
            return schoolRepository.save(created);
        });

        // Keep school metadata aligned with the dev preset.
        school.setPlatform(platform);
        school.setName(name);
        school.setBoard(board);
        school.setSubscriptionTier(tier);
        school.setEmail(adminEmail);
        school.setSubscriptionExpiry(LocalDate.now().plusYears(1));
        schoolRepository.save(school);

        // Admin user (always upsert password so login works even after old seeds)
        User admin = userRepository.findByEmail(adminEmail).orElseGet(User::new);
        admin.setSchool(school);
        admin.setEmail(adminEmail);
        admin.setPasswordHash(passwordEncoder.encode("Admin@1234"));
        admin.setRole(Role.SCHOOL_ADMIN);
        admin.setActive(true);
        userRepository.save(admin);

        // If students already exist, don't reinsert them (would violate unique constraints).
        if (studentRepository.countBySchoolId(school.getId()) > 0) {
            return;
        }

        // Teachers
        List<User> teachers = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            User teacher = userRepository.findByEmail("teacher" + i + "@" + code.toLowerCase() + ".edu.in")
                .orElseGet(User::new);
            teacher.setSchool(school);
            teacher.setEmail("teacher" + i + "@" + code.toLowerCase() + ".edu.in");
            teacher.setPasswordHash(passwordEncoder.encode("Admin@1234"));
            teacher.setRole(Role.TEACHER);
            teacher.setActive(true);
            teachers.add(teacher);
        }
        teachers = userRepository.saveAll(teachers);

        // Feature flags
        Set<FeatureKey> features = tier.defaultFeatures();
        List<FeatureFlag> flags = new ArrayList<>();
        for (FeatureKey key : features) {
            FeatureFlag ff = featureFlagRepository.findBySchoolIdAndFeatureKey(school.getId(), key)
                .orElseGet(FeatureFlag::new);
            ff.setSchool(school);
            ff.setFeatureKey(key);
            ff.setEnabled(true);
            flags.add(ff);
        }
        featureFlagRepository.saveAll(flags);

        AcademicYear currentYear = seedAcademicYear(school);
        List<SchoolClass> classes = seedClasses(school, teachers);
        seedSubjects(school);
        seedStudentsAndGuardians(school, currentYear, classes);

        log.info("Seeded school '{}' with {} features. Admin: {} / Admin@1234",
            name, features.size(), adminEmail);
    }

    private AcademicYear seedAcademicYear(School school) {
        AcademicYear year = new AcademicYear();
        year.setSchool(school);
        year.setName("2024-25");
        year.setStartDate(LocalDate.of(2024, 4, 1));
        year.setEndDate(LocalDate.of(2025, 3, 31));
        year.setCurrent(true);
        return academicYearRepository.save(year);
    }

    private List<SchoolClass> seedClasses(School school, List<User> teachers) {
        List<SchoolClass> classes = new ArrayList<>();
        int[] grades = {8, 9, 10};
        for (int i = 0; i < grades.length; i++) {
            SchoolClass schoolClass = new SchoolClass();
            schoolClass.setSchool(school);
            schoolClass.setGrade(grades[i]);
            schoolClass.setSection("A");
            schoolClass.setName("Grade " + grades[i] + "A");
            schoolClass.setCapacity(40);
            schoolClass.setClassTeacher(teachers.get(i % teachers.size()));
            classes.add(schoolClass);
        }
        return schoolClassRepository.saveAll(classes);
    }

    private void seedSubjects(School school) {
        seedSubject(school, "English", "ENG", 4);
        seedSubject(school, "Mathematics", "MATH", 5);
        seedSubject(school, "Science", "SCI", 5);
        seedSubject(school, "Social Studies", "SST", 4);
        seedSubject(school, "Hindi", "HIN", 4);
        seedSubject(school, "Computer", "COMP", 3);
    }

    private void seedSubject(School school, String name, String code, int credits) {
        Subject subject = new Subject();
        subject.setSchool(school);
        subject.setName(name);
        subject.setCode(code);
        subject.setType("CORE");
        subject.setCreditHours(credits);
        subjectRepository.save(subject);
    }

    private void seedStudentsAndGuardians(
        School school,
        AcademicYear academicYear,
        List<SchoolClass> classes
    ) {
        int studentCounter = 1;
        for (SchoolClass schoolClass : classes) {
            for (int i = 1; i <= 5; i++) {
                Student student = new Student();
                student.setSchool(school);
                student.setAcademicYear(academicYear);
                student.setSchoolClass(schoolClass);
                student.setAdmissionNo(school.getCode() + "-ADM-" + String.format("%03d", studentCounter));
                student.setRollNo(String.valueOf(i));
                student.setFirstName("Student" + studentCounter);
                student.setLastName("Demo");
                student.setDateOfBirth(LocalDate.of(2010, ((studentCounter % 12) + 1), ((studentCounter % 27) + 1)));
                student.setGender(studentCounter % 2 == 0 ? Gender.FEMALE : Gender.MALE);
                student.setCategory(StudentCategory.GEN);
                student.setAdmissionDate(LocalDate.of(2024, 4, 10));
                student.setActive(true);
                student.setMedicalConditions("{\"allergies\":[],\"notes\":\"none\"}");
                Student savedStudent = studentRepository.save(student);

                Guardian guardian = new Guardian();
                guardian.setStudent(savedStudent);
                guardian.setSchoolId(school.getId());
                guardian.setName("Parent " + studentCounter);
                guardian.setRelation("FATHER");
                guardian.setPhone("900000" + String.format("%04d", studentCounter));
                guardian.setEmail("parent" + studentCounter + "@" + school.getCode().toLowerCase() + ".edu.in");
                guardian.setOccupation("Service");
                guardian.setAddress("Sample Address, " + school.getName());
                guardian.setPrimary(true);
                guardian.setAuthorizedPickup(true);
                guardianRepository.save(guardian);

                studentCounter++;
            }
        }
    }

    private Platform getOrCreatePlatform() {
        return platformRepository.findByDomain("educloud.in").orElseGet(() -> {
            Platform platform = new Platform();
            platform.setName("EduCloud Platform");
            platform.setDomain("educloud.in");
            return platformRepository.save(platform);
        });
    }
}
