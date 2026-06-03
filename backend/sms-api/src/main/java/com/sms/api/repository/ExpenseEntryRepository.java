package com.sms.api.repository;

import com.sms.api.entity.ExpenseEntry;
import com.sms.core.enums.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExpenseEntryRepository extends JpaRepository<ExpenseEntry, UUID> {

    List<ExpenseEntry> findBySchoolIdAndExpenseDateBetweenOrderByExpenseDateDesc(
        UUID schoolId, LocalDate from, LocalDate to);

    List<ExpenseEntry> findBySchoolIdAndCategoryAndExpenseDateBetween(
        UUID schoolId, ExpenseCategory category, LocalDate from, LocalDate to);

    Optional<ExpenseEntry> findByIdAndSchoolId(UUID id, UUID schoolId);

    /** Total expenses for a school in a period — used in P&L */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM ExpenseEntry e " +
           "WHERE e.schoolId = :schoolId " +
           "AND e.expenseDate BETWEEN :from AND :to")
    BigDecimal sumBySchoolIdAndDateBetween(@Param("schoolId") UUID schoolId,
                                           @Param("from") LocalDate from,
                                           @Param("to") LocalDate to);

    /** Total expenses grouped by category — for P&L breakdown */
    @Query("SELECT e.category, COALESCE(SUM(e.amount), 0) FROM ExpenseEntry e " +
           "WHERE e.schoolId = :schoolId " +
           "AND e.expenseDate BETWEEN :from AND :to " +
           "GROUP BY e.category")
    List<Object[]> sumByCategory(@Param("schoolId") UUID schoolId,
                                  @Param("from") LocalDate from,
                                  @Param("to") LocalDate to);
}
