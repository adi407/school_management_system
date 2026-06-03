package com.sms.api.service;

import com.sms.api.dto.payroll.ProfitLossReportDto;
import com.sms.api.repository.ExpenseEntryRepository;
import com.sms.api.repository.FeePaymentRepository;
import com.sms.api.repository.PayrollRunRepository;
import com.sms.api.repository.PayslipRepository;
import com.sms.core.enums.ExpenseCategory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class FinanceReportService {

    private final FeePaymentRepository   feeRepo;
    private final PayrollRunRepository   runRepo;
    private final ExpenseEntryRepository expenseRepo;
    private final PayslipRepository      payslipRepo;

    public FinanceReportService(FeePaymentRepository feeRepo,
                                 PayrollRunRepository runRepo,
                                 ExpenseEntryRepository expenseRepo,
                                 PayslipRepository payslipRepo) {
        this.feeRepo     = feeRepo;
        this.runRepo     = runRepo;
        this.expenseRepo = expenseRepo;
        this.payslipRepo = payslipRepo;
    }

    /**
     * Generates a P&L report for a given month range within a single year.
     *
     * @param schoolId   the tenant
     * @param year       calendar year (e.g. 2025)
     * @param fromMonth  start month 1-12
     * @param toMonth    end month 1-12 (≥ fromMonth)
     */
    public ProfitLossReportDto generateReport(UUID schoolId, int year, int fromMonth, int toMonth) {
        LocalDate dateFrom = YearMonth.of(year, fromMonth).atDay(1);
        LocalDate dateTo   = YearMonth.of(year, toMonth).atEndOfMonth();

        // ── Revenue ───────────────────────────────────────────────────────────
        BigDecimal feeCollected = feeCollectedInPeriod(schoolId, dateFrom, dateTo);
        BigDecimal otherIncome  = BigDecimal.ZERO;   // extensible: grants, donations etc.
        BigDecimal totalRevenue = feeCollected.add(otherIncome);

        // ── Payroll cost (PAID runs only) ─────────────────────────────────────
        BigDecimal totalNetPayout = runRepo.sumNetPayoutForPeriod(schoolId, year, fromMonth, toMonth);
        if (totalNetPayout == null) totalNetPayout = BigDecimal.ZERO;

        // Employer PF & ESI from payslips in PAID runs for the period
        BigDecimal[] employerCosts = computeEmployerCosts(schoolId, year, fromMonth, toMonth);
        BigDecimal totalEmployerPf  = employerCosts[0];
        BigDecimal totalEmployerEsi = employerCosts[1];
        BigDecimal totalPayrollCost = totalNetPayout.add(totalEmployerPf).add(totalEmployerEsi);

        // ── Operational expenses ──────────────────────────────────────────────
        BigDecimal totalOpex = expenseRepo.sumBySchoolIdAndDateBetween(schoolId, dateFrom, dateTo);
        if (totalOpex == null) totalOpex = BigDecimal.ZERO;

        List<ProfitLossReportDto.ExpenseCategoryBreakdown> expenseBreakdown =
            buildExpenseBreakdown(schoolId, dateFrom, dateTo);

        // ── Totals ────────────────────────────────────────────────────────────
        BigDecimal totalExpenses = totalPayrollCost.add(totalOpex);
        BigDecimal netProfit     = totalRevenue.subtract(totalExpenses);
        BigDecimal margin = totalRevenue.compareTo(BigDecimal.ZERO) > 0
            ? netProfit.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        return new ProfitLossReportDto(
            fromMonth, toMonth, year,
            feeCollected, otherIncome, totalRevenue,
            totalNetPayout, totalEmployerPf, totalEmployerEsi, totalPayrollCost,
            totalOpex, expenseBreakdown,
            totalExpenses, netProfit, margin
        );
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private BigDecimal feeCollectedInPeriod(UUID schoolId, LocalDate from, LocalDate to) {
        // Reuse existing query from FeePaymentRepository — iterate by day range
        // The existing repo only has per-date and total; we build a range sum ourselves
        // by querying the payment table for all records in range
        BigDecimal sum = BigDecimal.ZERO;
        // Use the per-school total for now; a full-range query is added below via JPQL workaround
        // TODO: add a date-range sum to FeePaymentRepository if more granularity is needed
        // For now we approximate using existing queries:
        // Use a workaround: iterate dates is impractical, use sumTotalCollected is too broad.
        // Add a proper query:
        sum = feeRepo.sumCollectedInRange(schoolId, from, to);
        return sum != null ? sum : BigDecimal.ZERO;
    }

    private BigDecimal[] computeEmployerCosts(UUID schoolId, int year, int fromMonth, int toMonth) {
        // Sum employer PF and ESI from payslips belonging to PAID runs in this period
        BigDecimal pfEr  = BigDecimal.ZERO;
        BigDecimal esiEr = BigDecimal.ZERO;

        List<com.sms.api.entity.PayrollRun> paidRuns =
            runRepo.findBySchoolIdAndStatus(schoolId, com.sms.core.enums.PayrollStatus.PAID).stream()
                .filter(r -> r.getRunYear() == year && r.getRunMonth() >= fromMonth && r.getRunMonth() <= toMonth)
                .toList();

        for (var run : paidRuns) {
            pfEr  = pfEr.add(run.getTotalPfEmployer()  != null ? run.getTotalPfEmployer()  : BigDecimal.ZERO);
            esiEr = esiEr.add(run.getTotalEsiEmployer() != null ? run.getTotalEsiEmployer() : BigDecimal.ZERO);
        }
        return new BigDecimal[]{ pfEr, esiEr };
    }

    private List<ProfitLossReportDto.ExpenseCategoryBreakdown> buildExpenseBreakdown(
        UUID schoolId, LocalDate from, LocalDate to) {

        List<Object[]> rows = expenseRepo.sumByCategory(schoolId, from, to);
        Map<String, BigDecimal> byCategory = rows.stream()
            .collect(Collectors.toMap(
                r -> ((ExpenseCategory) r[0]).name(),
                r -> (BigDecimal) r[1]
            ));

        return Arrays.stream(ExpenseCategory.values())
            .filter(cat -> byCategory.containsKey(cat.name()))
            .map(cat -> new ProfitLossReportDto.ExpenseCategoryBreakdown(
                cat.name(), byCategory.get(cat.name())))
            .collect(Collectors.toList());
    }
}
