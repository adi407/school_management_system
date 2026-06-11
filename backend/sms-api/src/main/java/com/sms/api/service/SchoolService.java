package com.sms.api.service;

import com.sms.api.dto.school.CreateSchoolRequest;
import com.sms.api.dto.school.DeleteSchoolResponse;
import com.sms.api.dto.school.SchoolDto;
import com.sms.api.dto.school.UpdateSchoolRequest;
import com.sms.api.entity.FeatureFlag;
import com.sms.api.entity.Platform;
import com.sms.api.entity.School;
import com.sms.api.entity.User;
import com.sms.api.repository.FeatureFlagRepository;
import com.sms.api.repository.SchoolRepository;
import com.sms.api.repository.UserRepository;
import com.sms.core.enums.FeatureKey;
import com.sms.core.enums.Role;
import com.sms.core.exception.DuplicateResourceException;
import com.sms.core.exception.ResourceNotFoundException;
import org.springframework.context.annotation.Lazy;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class SchoolService {

    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;
    private final FeatureFlagRepository featureFlagRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;
    private final ModuleAssignmentService moduleAssignmentService;

    public SchoolService(
        SchoolRepository schoolRepository,
        UserRepository userRepository,
        FeatureFlagRepository featureFlagRepository,
        PasswordEncoder passwordEncoder,
        EntityManager entityManager,
        @Lazy ModuleAssignmentService moduleAssignmentService
    ) {
        this.schoolRepository      = schoolRepository;
        this.userRepository        = userRepository;
        this.featureFlagRepository = featureFlagRepository;
        this.passwordEncoder       = passwordEncoder;
        this.entityManager         = entityManager;
        this.moduleAssignmentService = moduleAssignmentService;
    }

    public Page<SchoolDto> listSchools(String search, String tier, Boolean isActive, Pageable pageable) {
        return schoolRepository.searchSchoolsNative(search, tier, isActive, pageable)
            .map(this::toDto);
    }

    public SchoolDto getSchool(UUID id) {
        return toDto(findOrThrow(id));
    }

    public SchoolDto createSchool(CreateSchoolRequest req) {
        if (schoolRepository.existsByCode(req.code())) {
            throw new DuplicateResourceException("School code '" + req.code() + "' already exists");
        }
        if (userRepository.existsByEmail(req.adminEmail())) {
            throw new DuplicateResourceException("Email '" + req.adminEmail() + "' already in use");
        }

        // Use first platform (seeded in DataSeederService)
        Platform platform = entityManager.createQuery("SELECT p FROM Platform p", Platform.class)
            .setMaxResults(1).getSingleResult();

        School school = new School();
        school.setPlatform(platform);
        school.setName(req.name());
        school.setCode(req.code().toUpperCase());
        school.setBoard(req.board());
        school.setSubscriptionTier(req.subscriptionTier());
        school.setAddress(req.address());
        school.setPhone(req.phone());
        school.setEmail(req.email());
        school.setTimezone(req.timezone() != null ? req.timezone() : "Asia/Kolkata");
        school.setLocale(req.locale() != null ? req.locale() : "en-IN");
        school.setSubscriptionExpiry(req.subscriptionExpiry());
        school = schoolRepository.save(school);

        // Create admin user
        User admin = new User();
        admin.setSchool(school);
        admin.setEmail(req.adminEmail());
        admin.setPasswordHash(passwordEncoder.encode(req.adminPassword()));
        admin.setRole(Role.SCHOOL_ADMIN);
        admin = userRepository.save(admin);

        // Auto-assign ALL modules to the founding admin (full access, null sub-permissions)
        moduleAssignmentService.assignAllModules(school.getId(), admin.getId());

        // Seed feature flags per subscription tier
        final School savedSchool = school;
        for (FeatureKey key : req.subscriptionTier().defaultFeatures()) {
            FeatureFlag ff = new FeatureFlag();
            ff.setSchool(savedSchool);
            ff.setFeatureKey(key);
            ff.setEnabled(true);
            featureFlagRepository.save(ff);
        }

        return toDto(school);
    }

    public SchoolDto updateSchool(UUID id, UpdateSchoolRequest req) {
        School school = findOrThrow(id);
        if (req.name()  != null) school.setName(req.name());
        if (req.board() != null) school.setBoard(req.board());
        if (req.address() != null) school.setAddress(req.address());
        if (req.phone()   != null) school.setPhone(req.phone());
        if (req.email()   != null) school.setEmail(req.email());
        if (req.timezone() != null) school.setTimezone(req.timezone());
        if (req.locale()   != null) school.setLocale(req.locale());
        if (req.subscriptionExpiry() != null) school.setSubscriptionExpiry(req.subscriptionExpiry());
        if (req.maxStudents() != null) school.setMaxStudents(req.maxStudents());
        if (req.maxStaff()    != null) school.setMaxStaff(req.maxStaff());
        if (req.subscriptionTier() != null) {
            school.setSubscriptionTier(req.subscriptionTier());
        }
        return toDto(schoolRepository.save(school));
    }

    public void setActive(UUID id, boolean active) {
        School school = findOrThrow(id);
        school.setActive(active);
        schoolRepository.save(school);
    }

    public DeleteSchoolResponse softDelete(UUID id) {
        School school = findOrThrow(id);

        // Deactivate the school
        school.setActive(false);
        schoolRepository.save(school);

        // Deactivate all users belonging to the school
        int usersAffected = entityManager.createNativeQuery(
                "UPDATE users SET is_active = false WHERE school_id = ?1")
            .setParameter(1, id)
            .executeUpdate();

        // Deactivate all students belonging to the school
        int studentsAffected = entityManager.createNativeQuery(
                "UPDATE students SET is_active = false WHERE school_id = ?1")
            .setParameter(1, id)
            .executeUpdate();

        return new DeleteSchoolResponse(
            school.getName(),
            "SOFT",
            usersAffected,
            studentsAffected,
            usersAffected + studentsAffected + 1 // +1 for the school itself
        );
    }

    public DeleteSchoolResponse hardDelete(UUID id) {
        School school = findOrThrow(id);
        String schoolName = school.getName();

        int totalRecords = 0;

        // --- Phase 1: Tables that reference school-owned records (no school_id column) ---

        // ptm_briefings → references ptm_meetings, students, users
        totalRecords += entityManager.createNativeQuery(
                "DELETE FROM ptm_briefings WHERE ptm_meeting_id IN (SELECT id FROM ptm_meetings WHERE school_id = ?1)")
            .setParameter(1, id).executeUpdate();

        // homework_submissions → references homework, students, users
        totalRecords += entityManager.createNativeQuery(
                "DELETE FROM homework_submissions WHERE homework_id IN (SELECT id FROM homework WHERE school_id = ?1)")
            .setParameter(1, id).executeUpdate();

        // book_issues → references books (via book_id) and students
        totalRecords += entityManager.createNativeQuery(
                "DELETE FROM book_issues WHERE book_id IN (SELECT id FROM books WHERE school_id = ?1)")
            .setParameter(1, id).executeUpdate();

        // notifications → references users
        totalRecords += entityManager.createNativeQuery(
                "DELETE FROM notifications WHERE user_id IN (SELECT id FROM users WHERE school_id = ?1)")
            .setParameter(1, id).executeUpdate();

        // password_reset_tokens → references users
        totalRecords += entityManager.createNativeQuery(
                "DELETE FROM password_reset_tokens WHERE user_id IN (SELECT id FROM users WHERE school_id = ?1)")
            .setParameter(1, id).executeUpdate();

        // refresh_tokens → references users
        totalRecords += entityManager.createNativeQuery(
                "DELETE FROM refresh_tokens WHERE user_id IN (SELECT id FROM users WHERE school_id = ?1)")
            .setParameter(1, id).executeUpdate();

        // --- Phase 2: Tables with school_id, ordered by FK dependencies (children first) ---

        // guardians → references students
        totalRecords += entityManager.createNativeQuery(
                "DELETE FROM guardians WHERE student_id IN (SELECT id FROM students WHERE school_id = ?1)")
            .setParameter(1, id).executeUpdate();

        // student_documents → references students
        totalRecords += entityManager.createNativeQuery(
                "DELETE FROM student_documents WHERE school_id = ?1")
            .setParameter(1, id).executeUpdate();

        // student_attendance → references students, school_classes
        totalRecords += entityManager.createNativeQuery(
                "DELETE FROM student_attendance WHERE school_id = ?1")
            .setParameter(1, id).executeUpdate();

        // achievements → references students, users
        totalRecords += entityManager.createNativeQuery(
                "DELETE FROM achievements WHERE school_id = ?1")
            .setParameter(1, id).executeUpdate();

        // attendance → references students, school_classes, users
        totalRecords += entityManager.createNativeQuery(
                "DELETE FROM attendance WHERE school_id = ?1")
            .setParameter(1, id).executeUpdate();

        // fee_payments → references students, users, fee_structures
        totalRecords += entityManager.createNativeQuery(
                "DELETE FROM fee_payments WHERE school_id = ?1")
            .setParameter(1, id).executeUpdate();

        // wellness_checkins → references students
        totalRecords += entityManager.createNativeQuery(
                "DELETE FROM wellness_checkins WHERE school_id = ?1")
            .setParameter(1, id).executeUpdate();

        // hostel_rooms → references hostels, students
        totalRecords += entityManager.createNativeQuery(
                "DELETE FROM hostel_rooms WHERE school_id = ?1")
            .setParameter(1, id).executeUpdate();

        // students (now safe — guardians, achievements, attendance, etc. removed)
        int studentsAffected = entityManager.createNativeQuery(
                "DELETE FROM students WHERE school_id = ?1")
            .setParameter(1, id).executeUpdate();
        totalRecords += studentsAffected;

        // --- Phase 3: Tables referencing school_classes, users, etc. ---

        // timetable_slots → references school_classes, subjects
        totalRecords += entityManager.createNativeQuery(
                "DELETE FROM timetable_slots WHERE school_id = ?1")
            .setParameter(1, id).executeUpdate();

        // substitute_assignments → references users
        totalRecords += entityManager.createNativeQuery(
                "DELETE FROM substitute_assignments WHERE school_id = ?1")
            .setParameter(1, id).executeUpdate();

        // class_subject_teachers → references class_subjects, users
        totalRecords += entityManager.createNativeQuery(
                "DELETE FROM class_subject_teachers WHERE school_id = ?1")
            .setParameter(1, id).executeUpdate();

        // class_subjects → references school_classes, subjects
        totalRecords += entityManager.createNativeQuery(
                "DELETE FROM class_subjects WHERE school_id = ?1")
            .setParameter(1, id).executeUpdate();

        // exams → references school_classes, exam_types, terms
        totalRecords += entityManager.createNativeQuery(
                "DELETE FROM exams WHERE school_id = ?1")
            .setParameter(1, id).executeUpdate();

        // fee_structures → references school_classes
        totalRecords += entityManager.createNativeQuery(
                "DELETE FROM fee_structures WHERE school_id = ?1")
            .setParameter(1, id).executeUpdate();

        // homework → references school_classes, subjects, users
        totalRecords += entityManager.createNativeQuery(
                "DELETE FROM homework WHERE school_id = ?1")
            .setParameter(1, id).executeUpdate();

        // ptm_meetings → references school_classes, users
        totalRecords += entityManager.createNativeQuery(
                "DELETE FROM ptm_meetings WHERE school_id = ?1")
            .setParameter(1, id).executeUpdate();

        // school_classes
        totalRecords += entityManager.createNativeQuery(
                "DELETE FROM school_classes WHERE school_id = ?1")
            .setParameter(1, id).executeUpdate();

        // --- Phase 4: Tables referencing users or other school-level entities ---

        // payslips → references users, payroll_runs
        totalRecords += entityManager.createNativeQuery(
                "DELETE FROM payslips WHERE school_id = ?1")
            .setParameter(1, id).executeUpdate();

        // payroll_runs → references users
        totalRecords += entityManager.createNativeQuery(
                "DELETE FROM payroll_runs WHERE school_id = ?1")
            .setParameter(1, id).executeUpdate();

        // salary_structures → references users
        totalRecords += entityManager.createNativeQuery(
                "DELETE FROM salary_structures WHERE school_id = ?1")
            .setParameter(1, id).executeUpdate();

        // staff_module_assignments → references staff, users
        totalRecords += entityManager.createNativeQuery(
                "DELETE FROM staff_module_assignments WHERE school_id = ?1")
            .setParameter(1, id).executeUpdate();

        // staff_leaves → references staff
        totalRecords += entityManager.createNativeQuery(
                "DELETE FROM staff_leaves WHERE school_id = ?1")
            .setParameter(1, id).executeUpdate();

        // staff_attendance → references staff
        totalRecords += entityManager.createNativeQuery(
                "DELETE FROM staff_attendance WHERE school_id = ?1")
            .setParameter(1, id).executeUpdate();

        // staff
        totalRecords += entityManager.createNativeQuery(
                "DELETE FROM staff WHERE school_id = ?1")
            .setParameter(1, id).executeUpdate();

        // grievances → references users
        totalRecords += entityManager.createNativeQuery(
                "DELETE FROM grievances WHERE school_id = ?1")
            .setParameter(1, id).executeUpdate();

        // expense_entries → references users
        totalRecords += entityManager.createNativeQuery(
                "DELETE FROM expense_entries WHERE school_id = ?1")
            .setParameter(1, id).executeUpdate();

        // route_stops → references routes
        totalRecords += entityManager.createNativeQuery(
                "DELETE FROM route_stops WHERE school_id = ?1")
            .setParameter(1, id).executeUpdate();

        // routes
        totalRecords += entityManager.createNativeQuery(
                "DELETE FROM routes WHERE school_id = ?1")
            .setParameter(1, id).executeUpdate();

        // hostels → references users (warden)
        totalRecords += entityManager.createNativeQuery(
                "DELETE FROM hostels WHERE school_id = ?1")
            .setParameter(1, id).executeUpdate();

        // books → references book_categories
        totalRecords += entityManager.createNativeQuery(
                "DELETE FROM books WHERE school_id = ?1")
            .setParameter(1, id).executeUpdate();

        // book_categories
        totalRecords += entityManager.createNativeQuery(
                "DELETE FROM book_categories WHERE school_id = ?1")
            .setParameter(1, id).executeUpdate();

        // --- Phase 5: Remaining school-level tables (no child dependencies left) ---

        totalRecords += entityManager.createNativeQuery(
                "DELETE FROM announcements WHERE school_id = ?1")
            .setParameter(1, id).executeUpdate();

        totalRecords += entityManager.createNativeQuery(
                "DELETE FROM activities WHERE school_id = ?1")
            .setParameter(1, id).executeUpdate();

        totalRecords += entityManager.createNativeQuery(
                "DELETE FROM events WHERE school_id = ?1")
            .setParameter(1, id).executeUpdate();

        totalRecords += entityManager.createNativeQuery(
                "DELETE FROM holiday_calendars WHERE school_id = ?1")
            .setParameter(1, id).executeUpdate();

        totalRecords += entityManager.createNativeQuery(
                "DELETE FROM exam_types WHERE school_id = ?1")
            .setParameter(1, id).executeUpdate();

        totalRecords += entityManager.createNativeQuery(
                "DELETE FROM subjects WHERE school_id = ?1")
            .setParameter(1, id).executeUpdate();

        totalRecords += entityManager.createNativeQuery(
                "DELETE FROM terms WHERE school_id = ?1")
            .setParameter(1, id).executeUpdate();

        totalRecords += entityManager.createNativeQuery(
                "DELETE FROM academic_years WHERE school_id = ?1")
            .setParameter(1, id).executeUpdate();

        totalRecords += entityManager.createNativeQuery(
                "DELETE FROM school_settings WHERE school_id = ?1")
            .setParameter(1, id).executeUpdate();

        totalRecords += entityManager.createNativeQuery(
                "DELETE FROM feature_flags WHERE school_id = ?1")
            .setParameter(1, id).executeUpdate();

        // --- Phase 6: Users (after all user-referencing tables are gone) ---

        int usersAffected = entityManager.createNativeQuery(
                "DELETE FROM users WHERE school_id = ?1")
            .setParameter(1, id).executeUpdate();
        totalRecords += usersAffected;

        // --- Phase 7: The school itself ---

        totalRecords += entityManager.createNativeQuery(
                "DELETE FROM schools WHERE id = ?1")
            .setParameter(1, id).executeUpdate();

        return new DeleteSchoolResponse(
            schoolName,
            "HARD",
            usersAffected,
            studentsAffected,
            totalRecords
        );
    }

    private School findOrThrow(UUID id) {
        return schoolRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("School", id));
    }

    private SchoolDto toDto(School s) {
        return new SchoolDto(
            s.getId(), s.getName(), s.getCode(), s.getBoard(),
            s.getSubscriptionTier(), s.getSubscriptionExpiry(),
            s.getAddress(), s.getPhone(), s.getEmail(), s.getLogoUrl(),
            s.getTimezone(), s.getLocale(), s.isActive(),
            0L, 0L, // TODO: count from StudentRepository/StaffRepository
            s.getCreatedAt(), s.getUpdatedAt()
        );
    }
}
