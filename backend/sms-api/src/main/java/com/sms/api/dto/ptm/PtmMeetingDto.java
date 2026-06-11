package com.sms.api.dto.ptm;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record PtmMeetingDto(
    UUID id,
    String title,
    UUID classId,
    String className,
    UUID academicYearId,
    LocalDate meetingDate,
    LocalTime startTime,
    LocalTime endTime,
    String status,
    String notes,
    String createdByName,
    int briefingCount
) {}
