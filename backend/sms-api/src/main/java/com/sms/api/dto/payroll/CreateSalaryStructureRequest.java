package com.sms.api.dto.payroll;

import com.sms.core.enums.TaxRegime;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateSalaryStructureRequest(

    @NotNull UUID staffId,

    @NotNull @DecimalMin("0.00") BigDecimal basicSalary,

    @DecimalMin("0.00") BigDecimal hraAmount,
    @DecimalMin("0.00") BigDecimal daAmount,
    @DecimalMin("0.00") BigDecimal taAmount,
    @DecimalMin("0.00") BigDecimal medicalAllowance,
    @DecimalMin("0.00") BigDecimal otherAllowances,

    boolean pfEnrolled,

    /** Override PF wage ceiling. Defaults to ₹15,000 if null. */
    BigDecimal pfWageCeiling,

    TaxRegime taxRegime,

    /** Annual 80C declared amount (old regime). Max ₹1,50,000. */
    @DecimalMin("0.00") BigDecimal declared80c,

    /** Annual HRA exemption declared (old regime). */
    @DecimalMin("0.00") BigDecimal declaredHraExemption,

    /** Other annual exemptions (NPS, insurance, etc.) */
    @DecimalMin("0.00") BigDecimal declaredOtherExemptions,

    @NotNull LocalDate effectiveFrom,
    LocalDate effectiveTo
) {}
