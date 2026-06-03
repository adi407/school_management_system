package com.sms.api.repository;

import com.sms.api.entity.User;
import com.sms.core.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findAllBySchoolIdAndRole(UUID schoolId, Role role);

    List<User> findAllBySchoolId(UUID schoolId);

    long countBySchoolIdAndIsActive(UUID schoolId, boolean isActive);

    /** Tenant-safe lookup — used by ModuleAssignmentService */
    Optional<User> findByIdAndSchoolId(UUID id, UUID schoolId);

    /** Count staff roles (non-SUPER_ADMIN, non-STUDENT, non-PARENT) */
    @org.springframework.data.jpa.repository.Query(
        "SELECT COUNT(u) FROM User u WHERE u.school.id = :schoolId " +
        "AND u.role IN ('TEACHER','ACCOUNTANT','LIBRARIAN','TRANSPORT_MANAGER','HOSTEL_WARDEN') " +
        "AND u.isActive = true")
    long countStaffBySchoolId(@org.springframework.data.repository.query.Param("schoolId") UUID schoolId);
}
