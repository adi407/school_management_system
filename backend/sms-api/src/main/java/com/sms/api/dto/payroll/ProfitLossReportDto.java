package com.sms.api.dto.payroll;

import java.math.BigDecimal;
import java.util.List;

public record ProfitLossReportDto(
    int    fromMonth,
    int    toMonth,
    int    year,

    // Revenue
    BigDecimal totalFeeCollected,
    BigDecimal otherIncome,
    BigDecimal totalRevenue,

    // Payroll costs
    BigDecimal totalSalaryPayout,         // sum of net salaries paid
    BigDecimal totalEmployerPf,           // employer PF liability
    BigDecimal totalEmployerEsi,          // employer ESI liability
    BigDecimal totalPayrollCost,          // salary + employer PF + ESI

    // Operating expenses
    BigDecimal totalOperationalExpenses,
    List<ExpenseCategoryBreakdown> expenseBreakdown,

    // Bottom line
    BigDecimal totalExpenses,             // payroll + operational
    BigDecimal netProfit,                 // revenue - totalExpenses
    BigDecimal netProfitMarginPct
) {
    public record ExpenseCategoryBreakdown(
        String     category,
        BigDecimal amount
    ) {}
}
