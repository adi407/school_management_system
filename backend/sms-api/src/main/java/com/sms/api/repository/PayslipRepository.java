package com.sms.api.repository;

import com.sms.api.entity.Payslip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayslipRepository extends JpaRepository<Payslip, UUID> {

    List<Payslip> findByPayrollRunId(UUID payrollRunId);

    List<Payslip> findByStaffIdAndSchoolId(UUID staffId, UUID schoolId);

    Optional<Payslip> findByPayrollRunIdAndStaffId(UUID payrollRunId, UUID staffId);

    Optional<Payslip> findByIdAndSchoolId(UUID id, UUID schoolId);

    /** All payslips for a staff member (most recent first) */
    @Query("SELECT p FROM Payslip p " +
           "JOIN p.payrollRun r " +
           "WHERE p.staff.id = :staffId " +
           "AND p.schoolId = :schoolId " +
           "ORDER BY r.runYear DESC, r.runMonth DESC")
    List<Payslip> findByStaffIdOrderByDateDesc(@Param("staffId") UUID staffId,
                                               @Param("schoolId") UUID schoolId);
}
