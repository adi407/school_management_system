package com.sms.api.repository;

import com.sms.api.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {

    /** Full roll for a class on a given date — teacher marks attendance */
    List<Attendance> findBySchoolClassIdAndAttendanceDateOrderByStudentId(UUID classId, LocalDate date);

    /** Single student record for a date (to detect duplicate) */
    Optional<Attendance> findByStudentIdAndAttendanceDate(UUID studentId, LocalDate date);

    /** Student history in a date range — for parent/student summary */
    List<Attendance> findByStudentIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(
        UUID studentId, LocalDate from, LocalDate to);

    /** Count by status in range — for attendance percentage */
    long countByStudentIdAndStatusAndAttendanceDateBetween(
        UUID studentId, String status, LocalDate from, LocalDate to);

    /** Total days recorded in range */
    long countByStudentIdAndAttendanceDateBetween(UUID studentId, LocalDate from, LocalDate to);

    /** All records for a class in a date range — for admin reports */
    @Query("SELECT a FROM Attendance a WHERE a.schoolClass.id = :classId " +
           "AND a.attendanceDate BETWEEN :from AND :to ORDER BY a.attendanceDate DESC")
    List<Attendance> findByClassIdAndDateRange(@Param("classId") UUID classId,
                                               @Param("from") LocalDate from,
                                               @Param("to") LocalDate to);

    /** School-wide total records on a date */
    long countBySchoolIdAndAttendanceDate(UUID schoolId, LocalDate date);

    /** School-wide PRESENT+LATE on a date */
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.schoolId = :schoolId " +
           "AND a.attendanceDate = :date AND a.status IN ('PRESENT','LATE')")
    long countPresentOnDate(@Param("schoolId") UUID schoolId, @Param("date") LocalDate date);
}
