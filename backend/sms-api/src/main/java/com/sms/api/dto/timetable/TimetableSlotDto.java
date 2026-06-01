package com.sms.api.dto.timetable;

import java.time.LocalTime;
import java.util.UUID;

public record TimetableSlotDto(
    UUID      id,
    UUID      classId,
    String    className,
    UUID      academicYearId,
    String    dayOfWeek,
    short     periodNo,
    UUID      subjectId,
    String    subjectName,
    UUID      teacherId,
    String    teacherName,
    LocalTime startTime,
    LocalTime endTime,
    String    roomNo
) {}
