package com.sms.api.dto.exam;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record CreateExamRequest(
    UUID      academicYearId,
    UUID      classId,          // null = all classes
    @NotBlank String    name,
    @NotBlank String    examType,
    @NotNull  LocalDate startDate,
    @NotNull  LocalDate endDate,
    int       totalSubjects,
    String    description,
    String    status
) {}
