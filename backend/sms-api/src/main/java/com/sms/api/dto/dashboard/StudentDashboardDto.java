package com.sms.api.dto.dashboard;

import com.sms.api.dto.attendance.AttendanceSummaryDto;
import com.sms.api.dto.homework.HomeworkDto;
import com.sms.api.dto.timetable.TimetableSlotDto;

import java.util.List;
import java.util.UUID;

public record StudentDashboardDto(
    UUID   studentId,
    String studentName,
    String admissionNo,
    String rollNo,
    UUID   classId,
    String className,
    AttendanceSummaryDto        attendance,
    List<HomeworkDto>           upcomingHomework,
    List<TimetableSlotDto>      todayTimetable
) {}
