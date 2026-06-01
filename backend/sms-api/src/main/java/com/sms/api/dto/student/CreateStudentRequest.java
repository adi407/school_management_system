package com.sms.api.dto.student;

import com.sms.core.enums.Gender;
import com.sms.core.enums.StudentCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateStudentRequest(

    // ── Core identity ─────────────────────────────────────
    @NotBlank  String firstName,
    @NotBlank  String lastName,
    @NotNull   LocalDate dateOfBirth,
    @NotNull   Gender gender,
    @NotNull   LocalDate admissionDate,

    // ── Academic placement ────────────────────────────────
    UUID    classId,
    UUID    academicYearId,
    String  rollNo,
    String  admissionNo,   // null ⇒ auto-generated as {CODE}-{YEAR}-{SEQ}

    // ── Personal details (all optional) ───────────────────
    String bloodGroup,
    String nationality,
    String religion,
    String caste,
    StudentCategory category,
    String motherTongue,
    String aadhaarNo,
    String houseGroup,
    String medicalConditions,
    String photoUrl,

    // ── Guardians (at least one required) ─────────────────
    @NotEmpty @Valid List<CreateGuardianRequest> guardians

) {}
