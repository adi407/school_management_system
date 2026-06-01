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
}
