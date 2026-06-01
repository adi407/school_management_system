package com.sms.api.dto.student;

import com.sms.core.enums.Gender;
import com.sms.core.enums.StudentCategory;

import java.time.LocalDate;
import java.util.UUID;

/**
 * All fields optional — only non-null fields are applied (partial update).
 */
public record UpdateStudentRequest(
    String firstName,
    String lastName,
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
    UUID academicYearId,
    String rollNo,
    String houseGroup,
    String medicalConditions,
    String photoUrl
) {}
