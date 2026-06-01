package com.sms.api.dto.academic;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateAcademicYearRequest(
    @NotBlank String name,        // e.g. "2026-27"
    @NotNull  LocalDate startDate,
    @NotNull  LocalDate endDate,
    boolean   isCurrent           // if true, clears isCurrent from all others
) {}
