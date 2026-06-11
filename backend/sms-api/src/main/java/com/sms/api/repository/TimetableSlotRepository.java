package com.sms.api.repository;

import com.sms.api.entity.TimetableSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TimetableSlotRepository extends JpaRepository<TimetableSlot, UUID> {

    List<TimetableSlot> findBySchoolClassIdAndAcademicYearIdOrderByDayOfWeekAscPeriodNoAsc(
        UUID classId, UUID academicYearId);

    List<TimetableSlot> findByTeacherIdAndAcademicYearIdOrderByDayOfWeekAscPeriodNoAsc(
        UUID teacherId, UUID academicYearId);

    Optional<TimetableSlot> findBySchoolClassIdAndDayOfWeekAndPeriodNo(
        UUID classId, String dayOfWeek, int periodNo);

    /** All slots for a teacher on a specific day */
    List<TimetableSlot> findByTeacherIdAndDayOfWeekOrderByPeriodNoAsc(UUID teacherId, String dayOfWeek);

    /** All slots for a specific day across the school (for finding free teachers) */
    @org.springframework.data.jpa.repository.Query(
        "SELECT ts FROM TimetableSlot ts WHERE ts.schoolId = :schoolId " +
        "AND ts.dayOfWeek = :day ORDER BY ts.periodNo ASC")
    List<TimetableSlot> findAllBySchoolAndDay(
        @org.springframework.data.repository.query.Param("schoolId") UUID schoolId,
        @org.springframework.data.repository.query.Param("day") String day);
}
