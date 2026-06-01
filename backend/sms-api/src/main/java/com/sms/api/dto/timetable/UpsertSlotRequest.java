package com.sms.api.dto.timetable;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;
import java.util.UUID;

public record UpsertSlotRequest(
    @NotNull UUID      classId,
    @NotNull UUID      academicYearId,
    @NotBlank String   dayOfWeek,       // MON | TUE | WED | THU | FRI | SAT
    @NotNull @Min(1) @Max(12) short periodNo,
    UUID               subjectId,
    UUID               teacherId,
    @NotNull LocalTime startTime,
    @NotNull LocalTime endTime,
    String             roomNo
) {}
