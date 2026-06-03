package com.sms.api.dto.payroll;

import java.time.LocalDate;
import java.util.UUID;

public record StaffAttendanceDto(
    UUID      id,
    UUID      staffId,
    String    staffName,
    String    staffEmail,
    LocalDate attendanceDate,
    String    status,
    String    remarks
) {}
