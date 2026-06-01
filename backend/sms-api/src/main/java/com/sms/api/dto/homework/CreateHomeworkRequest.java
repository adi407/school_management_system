package com.sms.api.dto.homework;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record CreateHomeworkRequest(
    UUID      classId,            // null = school-wide broadcast to all classes
    UUID      subjectId,
    UUID      academicYearId,
    @NotBlank String    title,
    @NotBlank String    description,
    @NotNull  @Future LocalDate dueDate,
    Integer   estimatedMinutes,
    boolean   isPublished,        // default true; false = save as draft
    boolean   isSchoolWide        // true = no class filter, visible to everyone
) {}
