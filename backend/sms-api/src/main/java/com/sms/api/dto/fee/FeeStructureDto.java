package com.sms.api.dto.fee;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record FeeStructureDto(
    UUID      id,
    UUID      classId,
    String    className,
    UUID      academicYearId,
    String    academicYearName,
    String    feeType,
    BigDecimal amount,
    LocalDate dueDate,
    boolean   isRecurring,
    String    frequency,
    String    description
) {}
