package com.sms.api.repository;

import com.sms.api.entity.WellnessCheckin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WellnessCheckinRepository extends JpaRepository<WellnessCheckin, UUID> {

    /** Check if student already checked in today */
    Optional<WellnessCheckin> findByStudentIdAndCheckinDate(UUID studentId, LocalDate date);

    /** All check-ins for a class on a date (for teacher/counselor aggregate) */
    List<WellnessCheckin> findBySchoolClassIdAndCheckinDate(UUID classId, LocalDate date);

    /** School-wide check-ins on a date */
    List<WellnessCheckin> findBySchoolIdAndCheckinDate(UUID schoolId, LocalDate date);

    /** Mood trend for a class over a date range */
    @Query("SELECT w FROM WellnessCheckin w WHERE w.schoolClass.id = :classId " +
           "AND w.checkinDate BETWEEN :from AND :to ORDER BY w.checkinDate DESC")
    List<WellnessCheckin> findClassTrend(UUID classId, LocalDate from, LocalDate to);

    /** Count negative moods (SAD/STRESSED) for a class on a date — for alert trigger */
    @Query("SELECT COUNT(w) FROM WellnessCheckin w WHERE w.schoolClass.id = :classId " +
           "AND w.checkinDate = :date AND w.mood IN ('SAD','STRESSED')")
    long countNegativeMoods(UUID classId, LocalDate date);

    /** Total check-ins for a class on a date */
    long countBySchoolClassIdAndCheckinDate(UUID classId, LocalDate date);
}
