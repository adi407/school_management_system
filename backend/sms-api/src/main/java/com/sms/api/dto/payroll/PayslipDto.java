package com.sms.api.dto.payroll;

import java.math.BigDecimal;
import java.util.UUID;

public record PayslipDto(
    UUID   id,
    UUID   payrollRunId,
    int    runMonth,
    int    runYear,

    // Staff
    UUID   staffId,
    String staffName,
    String staffEmail,
    String staffRole,
    String department,

    // Earnings
    BigDecimal basicSalary,
    BigDecimal hraAmount,
    BigDecimal daAmount,
    BigDecimal taAmount,
    BigDecimal medicalAllowance,
    BigDecimal otherAllowances,
    BigDecimal grossSalary,

    // LOP
    int        totalWorkingDays,
    int        presentDays,
    int        lopDays,
    BigDecimal lopDeduction,
    BigDecimal effectiveGross,

    // Deductions
    BigDecimal pfEmployee,
    BigDecimal pfEmployer,
    BigDecimal esiEmployee,
    BigDecimal esiEmployer,
    BigDecimal professionalTax,
    BigDecimal tds,
    BigDecimal totalDeductions,

    // Net
    BigDecimal netSalary
) {}
