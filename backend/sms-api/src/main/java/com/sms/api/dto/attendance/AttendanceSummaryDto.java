package com.sms.api.dto.attendance;

import java.util.UUID;

/** Attendance percentage summary for a student — shown on dashboards. */
public record AttendanceSummaryDto(
    UUID   studentId,
    String studentName,
    long   totalDays,
    long   presentDays,
    long   absentDays,
    long   lateDays,
    double attendancePercent   // (present + late) / total * 100
) {}
