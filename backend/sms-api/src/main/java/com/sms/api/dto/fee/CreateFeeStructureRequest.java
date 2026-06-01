package com.sms.api.dto.fee;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateFeeStructureRequest(
    UUID      classId,            // null = applies to all classes
    UUID      academicYearId,
    @NotBlank String    feeType,
    @NotNull @DecimalMin("0.01") BigDecimal amount,
    LocalDate dueDate,
    boolean   isRecurring,
    String    frequency,          // MONTHLY | QUARTERLY | ANNUAL | ONE_TIME
    String    description
) {}
