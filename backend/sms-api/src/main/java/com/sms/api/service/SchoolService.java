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

        // Count before deleting
        Long usersCount = ((Number) entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM users WHERE school_id = ?1")
            .setParameter(1, id).getSingleResult()).longValue();
        Long studentsCount = ((Number) entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM students WHERE school_id = ?1")
            .setParameter(1, id).getSingleResult()).longValue();

        // Use a PostgreSQL DO block for cascade delete.
        // Tables without JPA entities may not exist on production (ddl-auto:update),
        // so they are wrapped in BEGIN...EXCEPTION WHEN undefined_table.
        String idStr = id.toString(); // UUID is safe — validated by findOrThrow
        entityManager.createNativeQuery(
            "DO $$ DECLARE sid UUID := '" + idStr + "'; BEGIN " +

            // Phase 1: junction/child tables with no school_id (reference via sub-query)
            "DELETE FROM ptm_briefings WHERE ptm_meeting_id IN (SELECT id FROM ptm_meetings WHERE school_id = sid); " +
            "DELETE FROM homework_submissions WHERE homework_id IN (SELECT id FROM homework WHERE school_id = sid); " +
            "DELETE FROM book_issues WHERE book_id IN (SELECT id FROM books WHERE school_id = sid); " +
            "BEGIN DELETE FROM notifications WHERE user_id IN (SELECT id FROM users WHERE school_id = sid); EXCEPTION WHEN undefined_table THEN NULL; END; " +
            "DELETE FROM password_reset_tokens WHERE user_id IN (SELECT id FROM users WHERE school_id = sid); " +
            "DELETE FROM refresh_tokens WHERE user_id IN (SELECT id FROM users WHERE school_id = sid); " +

            // Phase 2: student-related
            "DELETE FROM guardians WHERE student_id IN (SELECT id FROM students WHERE school_id = sid); " +
            "BEGIN DELETE FROM student_documents WHERE school_id = sid; EXCEPTION WHEN undefined_table THEN NULL; END; " +
            "BEGIN DELETE FROM student_attendance WHERE school_id = sid; EXCEPTION WHEN undefined_table THEN NULL; END; " +
            "BEGIN DELETE FROM achievements WHERE school_id = sid; EXCEPTION WHEN undefined_table THEN NULL; END; " +
            "DELETE FROM attendance WHERE school_id = sid; " +
            "DELETE FROM fee_payments WHERE school_id = sid; " +
            "DELETE FROM wellness_checkins WHERE school_id = sid; " +
            "BEGIN DELETE FROM hostel_rooms WHERE school_id = sid; EXCEPTION WHEN undefined_table THEN NULL; END; " +
            "DELETE FROM students WHERE school_id = sid; " +

            // Phase 3: class/academic
            "DELETE FROM timetable_slots WHERE school_id = sid; " +
            "DELETE FROM substitute_assignments WHERE school_id = sid; " +
            "DELETE FROM class_subject_teachers WHERE school_id = sid; " +
            "BEGIN DELETE FROM class_subjects WHERE school_id = sid; EXCEPTION WHEN undefined_table THEN NULL; END; " +
            "DELETE FROM exams WHERE school_id = sid; " +
            "DELETE FROM fee_structures WHERE school_id = sid; " +
            "DELETE FROM homework WHERE school_id = sid; " +
            "DELETE FROM ptm_meetings WHERE school_id = sid; " +
            "DELETE FROM school_classes WHERE school_id = sid; " +

            // Phase 4: staff/payroll/misc
            "DELETE FROM payslips WHERE school_id = sid; " +
            "DELETE FROM payroll_runs WHERE school_id = sid; " +
            "DELETE FROM salary_structures WHERE school_id = sid; " +
            "DELETE FROM staff_module_assignments WHERE school_id = sid; " +
            "BEGIN DELETE FROM staff_leaves WHERE school_id = sid; EXCEPTION WHEN undefined_table THEN NULL; END; " +
            "DELETE FROM staff_attendance WHERE school_id = sid; " +
            "BEGIN DELETE FROM staff WHERE school_id = sid; EXCEPTION WHEN undefined_table THEN NULL; END; " +
            "BEGIN DELETE FROM grievances WHERE school_id = sid; EXCEPTION WHEN undefined_table THEN NULL; END; " +
            "DELETE FROM expense_entries WHERE school_id = sid; " +
            "BEGIN DELETE FROM route_stops WHERE school_id = sid; EXCEPTION WHEN undefined_table THEN NULL; END; " +
            "BEGIN DELETE FROM routes WHERE school_id = sid; EXCEPTION WHEN undefined_table THEN NULL; END; " +
            "BEGIN DELETE FROM hostels WHERE school_id = sid; EXCEPTION WHEN undefined_table THEN NULL; END; " +
            "DELETE FROM books WHERE school_id = sid; " +
            "BEGIN DELETE FROM book_categories WHERE school_id = sid; EXCEPTION WHEN undefined_table THEN NULL; END; " +

            // Phase 5: school-level config
            "DELETE FROM announcements WHERE school_id = sid; " +
            "DELETE FROM activities WHERE school_id = sid; " +
            "BEGIN DELETE FROM events WHERE school_id = sid; EXCEPTION WHEN undefined_table THEN NULL; END; " +
            "BEGIN DELETE FROM holiday_calendars WHERE school_id = sid; EXCEPTION WHEN undefined_table THEN NULL; END; " +
            "BEGIN DELETE FROM exam_types WHERE school_id = sid; EXCEPTION WHEN undefined_table THEN NULL; END; " +
            "DELETE FROM subjects WHERE school_id = sid; " +
            "BEGIN DELETE FROM terms WHERE school_id = sid; EXCEPTION WHEN undefined_table THEN NULL; END; " +
            "DELETE FROM academic_years WHERE school_id = sid; " +
            "BEGIN DELETE FROM school_settings WHERE school_id = sid; EXCEPTION WHEN undefined_table THEN NULL; END; " +
            "DELETE FROM feature_flags WHERE school_id = sid; " +
            "BEGIN DELETE FROM audit_logs WHERE school_id = sid; EXCEPTION WHEN undefined_table THEN NULL; END; " +

            // Phase 6: users, Phase 7: school
            "DELETE FROM users WHERE school_id = sid; " +
            "DELETE FROM schools WHERE id = sid; " +

            "END $$;"
        ).executeUpdate();

        return new DeleteSchoolResponse(
            schoolName,
            "HARD",
            usersCount.intValue(),
            studentsCount.intValue(),
            usersCount.intValue() + studentsCount.intValue() + 1
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
