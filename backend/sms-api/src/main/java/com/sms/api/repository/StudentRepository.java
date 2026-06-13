package com.sms.api.repository;

import com.sms.api.entity.Student;
import com.sms.core.enums.Gender;
import com.sms.core.enums.StudentCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudentRepository extends JpaRepository<Student, UUID> {

    // :search must never be null — pass "" from service to match everything via '%%'
    @Query("SELECT s FROM Student s WHERE s.schoolId = :schoolId AND " +
           "(:classId IS NULL OR s.schoolClass.id = :classId) AND " +
           "(:gender IS NULL OR s.gender = :gender) AND " +
           "(:category IS NULL OR s.category = :category) AND " +
           "(:isActive IS NULL OR s.isActive = :isActive) AND " +
           "(LOWER(CONCAT(s.firstName, ' ', s.lastName)) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(s.admissionNo) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Student> searchStudents(
        UUID schoolId, UUID classId, Gender gender,
        StudentCategory category, Boolean isActive, String search,
        Pageable pageable
    );

    Optional<Student> findBySchoolIdAndAdmissionNo(UUID schoolId, String admissionNo);

    boolean existsBySchoolIdAndAdmissionNo(UUID schoolId, String admissionNo);

    long countBySchoolIdAndIsActive(UUID schoolId, boolean isActive);

    long countBySchoolClassId(UUID classId);

    long countBySchoolId(UUID schoolId);

    Optional<Student> findByIdAndSchoolId(UUID id, UUID schoolId);

    List<Student> findBySchoolClassIdAndSchoolIdAndIsActiveTrue(UUID classId, UUID schoolId);

    List<Student> findBySchoolIdAndIsActiveTrue(UUID schoolId);

    /** All active students in a class — used by PTM briefing generation */
    List<Student> findBySchoolClassIdAndIsActiveTrueOrderByFirstNameAsc(UUID classId);

    /** Resolve a Student from their linked User account — used by the submit endpoint */
    @Query("SELECT s FROM Student s WHERE s.user.id = :userId AND s.schoolId = :schoolId")
    Optional<Student> findByUserIdAndSchoolId(@org.springframework.data.repository.query.Param("userId") UUID userId,
                                              @org.springframework.data.repository.query.Param("schoolId") UUID schoolId);
}
