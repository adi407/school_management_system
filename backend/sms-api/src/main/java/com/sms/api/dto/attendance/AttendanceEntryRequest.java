package com.sms.api.dto.attendance;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/** One student's status within a bulk mark-attendance call. */
public record AttendanceEntryRequest(
    @NotNull UUID   studentId,
    @NotNull String status,    // PRESENT | ABSENT | LATE | EXCUSED
    String          remarks
) {}
