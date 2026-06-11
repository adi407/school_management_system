package com.sms.api.repository;

import com.sms.api.entity.SubstituteAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubstituteAssignmentRepository extends JpaRepository<SubstituteAssignment, UUID> {

    List<SubstituteAssignment> findBySchoolIdAndAbsenceDateOrderByPeriodNoAsc(UUID schoolId, LocalDate date);

    List<SubstituteAssignment> findByAbsentTeacherIdAndAbsenceDateOrderByPeriodNoAsc(UUID teacherId, LocalDate date);

    List<SubstituteAssignment> findBySubstituteTeacherIdAndAbsenceDateOrderByPeriodNoAsc(UUID teacherId, LocalDate date);

    Optional<SubstituteAssignment> findBySchoolClassIdAndPeriodNoAndAbsenceDate(UUID classId, short periodNo, LocalDate date);

    /** Count how many substitute periods a teacher is already covering on a given date */
    long countBySubstituteTeacherIdAndAbsenceDate(UUID teacherId, LocalDate date);

    /** All pending/suggested assignments for a date (admin review) */
    @Query("SELECT sa FROM SubstituteAssignment sa WHERE sa.schoolId = :schoolId " +
           "AND sa.absenceDate = :date AND sa.status IN ('PENDING', 'SUGGESTED') " +
           "ORDER BY sa.periodNo ASC")
    List<SubstituteAssignment> findPendingBySchoolAndDate(
        @Param("schoolId") UUID schoolId, @Param("date") LocalDate date);
}
