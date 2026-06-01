package com.sms.api.dto.attendance;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Bulk payload: teacher submits a full class roll for one date. */
public record MarkAttendanceRequest(
    @NotNull UUID      classId,
    @NotNull LocalDate date,
    @NotNull @Valid List<AttendanceEntryRequest> entries
) {}
