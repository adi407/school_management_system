package com.sms.api.dto.payroll;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record TriggerPayrollRequest(
    @NotNull @Min(1) @Max(12) Integer month,
    @NotNull @Min(2020) Integer year,

    /** Number of working days for this month. Defaults to 26 if not supplied. */
    @Min(1) @Max(31) Integer totalWorkingDays,

    String notes
) {}
