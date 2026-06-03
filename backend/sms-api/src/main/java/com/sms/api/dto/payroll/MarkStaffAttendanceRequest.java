package com.sms.api.dto.payroll;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record MarkStaffAttendanceRequest(
    @NotNull LocalDate date,
    @NotEmpty @Valid List<StaffAttendanceEntry> entries
) {
    public record StaffAttendanceEntry(
        @NotNull UUID staffId,
        /** PRESENT | ABSENT | HALF_DAY | HOLIDAY | LEAVE */
        @NotNull String status,
        String remarks
    ) {}
}
