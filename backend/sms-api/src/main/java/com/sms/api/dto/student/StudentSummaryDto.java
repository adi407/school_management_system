package com.sms.api.dto.student;

import com.sms.core.enums.Gender;
import com.sms.core.enums.StudentCategory;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Lightweight projection used in paginated student lists.
 */
public record StudentSummaryDto(
    UUID id,
    String admissionNo,
    String rollNo,
    String firstName,
    String lastName,
    String fullName,
    LocalDate dateOfBirth,
    Gender gender,
    StudentCategory category,
    UUID classId,
    String className,
    boolean isActive,
    LocalDate admissionDate,
    String photoUrl,
    Instant createdAt
) {}
