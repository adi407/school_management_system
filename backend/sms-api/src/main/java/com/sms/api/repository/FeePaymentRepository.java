package com.sms.api.repository;

import com.sms.api.entity.FeePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface FeePaymentRepository extends JpaRepository<FeePayment, UUID> {

    List<FeePayment> findByStudentIdOrderByPaymentDateDesc(UUID studentId);

    List<FeePayment> findBySchoolIdOrderByPaymentDateDesc(UUID schoolId);

    boolean existsByReceiptNo(String receiptNo);

    @Query("SELECT COALESCE(SUM(p.amountPaid), 0) FROM FeePayment p WHERE p.student.id = :studentId")
    BigDecimal sumPaidByStudent(@Param("studentId") UUID studentId);

    @Query("SELECT COALESCE(SUM(p.amountPaid), 0) FROM FeePayment p WHERE p.schoolId = :schoolId AND p.paymentDate = :date")
    BigDecimal sumCollectedOnDate(@Param("schoolId") UUID schoolId, @Param("date") java.time.LocalDate date);

    @Query("SELECT COALESCE(SUM(p.amountPaid), 0) FROM FeePayment p WHERE p.schoolId = :schoolId")
    BigDecimal sumTotalCollected(@Param("schoolId") UUID schoolId);

    List<FeePayment> findTop5BySchoolIdOrderByPaymentDateDescIdDesc(UUID schoolId);

    @Query("SELECT COALESCE(SUM(p.amountPaid), 0) FROM FeePayment p " +
           "WHERE p.schoolId = :schoolId " +
           "AND p.paymentDate BETWEEN :from AND :to")
    BigDecimal sumCollectedInRange(@Param("schoolId") UUID schoolId,
                                   @Param("from") java.time.LocalDate from,
                                   @Param("to") java.time.LocalDate to);
}
