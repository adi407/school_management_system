package com.sms.api.dto.substitute;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record SubstituteAssignmentDto(
    UUID id,
    UUID absentTeacherId,
    String absentTeacherName,
    UUID substituteTeacherId,
    String substituteTeacherName,
    LocalDate absenceDate,
    short periodNo,
    UUID classId,
    String className,
    UUID subjectId,
    String subjectName,
    LocalTime startTime,
    LocalTime endTime,
    String status,
    String suggestionReason,
    Float confidenceScore,
    String remarks
) {}
