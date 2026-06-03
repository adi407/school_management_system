package com.sms.api.repository;

import com.sms.api.entity.PayrollRun;
import com.sms.core.enums.PayrollStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayrollRunRepository extends JpaRepository<PayrollRun, UUID> {

    List<PayrollRun> findBySchoolIdOrderByRunYearDescRunMonthDesc(UUID schoolId);

    Optional<PayrollRun> findBySchoolIdAndRunMonthAndRunYear(UUID schoolId, int month, int year);

    Optional<PayrollRun> findByIdAndSchoolId(UUID id, UUID schoolId);

    List<PayrollRun> findBySchoolIdAndStatus(UUID schoolId, PayrollStatus status);

    /** Aggregate total net payout across PAID runs for P&L */
    @Query("SELECT COALESCE(SUM(r.totalNetPayout), 0) FROM PayrollRun r " +
           "WHERE r.schoolId = :schoolId " +
           "AND r.status = 'PAID' " +
           "AND r.runYear = :year " +
           "AND r.runMonth BETWEEN :fromMonth AND :toMonth")
    java.math.BigDecimal sumNetPayoutForPeriod(@Param("schoolId") UUID schoolId,
                                               @Param("year") int year,
                                               @Param("fromMonth") int fromMonth,
                                               @Param("toMonth") int toMonth);
}
