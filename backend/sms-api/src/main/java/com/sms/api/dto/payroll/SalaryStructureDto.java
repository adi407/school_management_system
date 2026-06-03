package com.sms.api.dto.payroll;

import com.sms.core.enums.TaxRegime;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record SalaryStructureDto(
    UUID   id,
    UUID   staffId,
    String staffName,
    String staffEmail,
    String staffRole,

    // Salary components
    BigDecimal basicSalary,
    BigDecimal hraAmount,
    BigDecimal daAmount,
    BigDecimal taAmount,
    BigDecimal medicalAllowance,
    BigDecimal otherAllowances,
    BigDecimal grossSalary,        // computed

    // PF settings
    boolean    pfEnrolled,
    BigDecimal pfWageCeiling,

    // Tax & declarations
    TaxRegime  taxRegime,
    BigDecimal declared80c,
    BigDecimal declaredHraExemption,
    BigDecimal declaredOtherExemptions,

    // Effective period
    LocalDate  effectiveFrom,
    LocalDate  effectiveTo,
    boolean    isActive
) {}
