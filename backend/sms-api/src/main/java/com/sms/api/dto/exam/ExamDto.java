package com.sms.api.dto.exam;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ExamDto(
    UUID      id,
    UUID      schoolId,
    UUID      academicYearId,
    String    academicYearName,
    UUID      classId,
    String    className,
    String    name,
    String    examType,
    LocalDate startDate,
    LocalDate endDate,
    int       totalSubjects,
    String    description,
    String    status,
    Instant   createdAt
) {}
