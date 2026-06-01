package com.sms.api.dto.attendance;

import java.time.LocalDate;
import java.util.UUID;

/** Single attendance record — returned in roll call and history views. */
public record AttendanceRecordDto(
    UUID      id,
    UUID      studentId,
    String    studentName,
    String    admissionNo,
    UUID      classId,
    String    className,
    LocalDate attendanceDate,
    String    status,
    String    remarks,
    UUID      markedById,
    String    markedByEmail
) {}
