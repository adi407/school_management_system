package com.sms.api.dto.dashboard;

import com.sms.api.dto.timetable.TimetableSlotDto;
import com.sms.api.dto.homework.HomeworkDto;

import java.util.List;

public record TeacherDashboardDto(
    String               teacherName,
    List<TimetableSlotDto> todaySchedule,
    List<HomeworkDto>    pendingHomework,
    long                 totalHomeworkAssigned,
    long                 classesTeachingToday
) {}
