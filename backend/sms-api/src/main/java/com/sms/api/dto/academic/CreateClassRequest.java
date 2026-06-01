package com.sms.api.dto.academic;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateClassRequest(
    @NotNull  @Min(1) @Max(12) int grade,
    @NotBlank String section,     // A, B, C …
    @NotBlank String name,        // e.g. "Grade 9 - A"
    @Min(1)   int capacity,
    UUID      classTeacherId      // optional
) {}
