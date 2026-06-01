package com.sms.api.dto.academic;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AcademicYearDto(
    UUID id,
    UUID schoolId,
    String name,
    LocalDate startDate,
    LocalDate endDate,
    boolean isCurrent,
    Instant createdAt
) {}
