package com.sms.api.dto.student;

import com.sms.core.enums.Gender;
import com.sms.core.enums.StudentCategory;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Full student record returned by GET /students/{id}.
 */
public record StudentDto(
    UUID id,
    UUID schoolId,
    String admissionNo,
    String rollNo,
    String firstName,
    String lastName,
    String fullName,
    LocalDate dateOfBirth,
    Gender gender,
    String bloodGroup,
    String nationality,
    String religion,
    String caste,
    StudentCategory category,
    String motherTongue,
    String aadhaarNo,
    UUID classId,
    String className,
    UUID academicYearId,
    String academicYearName,
    String houseGroup,
    boolean isActive,
    LocalDate admissionDate,
    boolean tcIssued,
    String photoUrl,
    String medicalConditions,
    List<GuardianDto> guardians,
    Instant createdAt,
    Instant updatedAt
) {}
