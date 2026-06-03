package com.sms.api.repository;

import com.sms.api.entity.StaffAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StaffAttendanceRepository extends JpaRepository<StaffAttendance, UUID> {

    Optional<StaffAttendance> findByStaffIdAndAttendanceDate(UUID staffId, LocalDate date);

    List<StaffAttendance> findBySchoolIdAndAttendanceDateOrderByStaffId(UUID schoolId, LocalDate date);

    List<StaffAttendance> findByStaffIdAndAttendanceDateBetweenOrderByAttendanceDateAsc(
        UUID staffId, LocalDate from, LocalDate to);

    /** Count PRESENT + HALF_DAY days for a staff member in a month-range */
    @Query("SELECT COUNT(sa) FROM StaffAttendance sa " +
           "WHERE sa.staff.id = :staffId " +
           "AND sa.attendanceDate BETWEEN :from AND :to " +
           "AND sa.status IN ('PRESENT', 'HALF_DAY')")
    long countPresentDays(@Param("staffId") UUID staffId,
                          @Param("from") LocalDate from,
                          @Param("to") LocalDate to);

    /** Count HALF_DAY specifically (counts as 0.5 — handled in service) */
    @Query("SELECT COUNT(sa) FROM StaffAttendance sa " +
           "WHERE sa.staff.id = :staffId " +
           "AND sa.attendanceDate BETWEEN :from AND :to " +
           "AND sa.status = 'HALF_DAY'")
    long countHalfDays(@Param("staffId") UUID staffId,
                       @Param("from") LocalDate from,
                       @Param("to") LocalDate to);
}
