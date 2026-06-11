package com.sms.api.dto.ptm;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record CreatePtmRequest(
    @NotBlank String title,
    UUID classId,           // null = school-wide
    @NotNull UUID academicYearId,
    @NotNull LocalDate meetingDate,
    LocalTime startTime,
    LocalTime endTime,
    String notes
) {}
