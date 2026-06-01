package com.sms.api.dto.dashboard;

import com.sms.api.dto.announcement.AnnouncementDto;
import com.sms.api.dto.attendance.AttendanceSummaryDto;
import com.sms.api.dto.fee.StudentFeesSummaryDto;
import com.sms.api.dto.homework.HomeworkDto;

import java.util.List;
import java.util.UUID;

public record ParentDashboardDto(
    UUID   studentId,
    String studentName,
    String admissionNo,
    String rollNo,
    UUID   classId,
    String className,
    String guardianName,
    String guardianRelation,
    AttendanceSummaryDto   attendance,
    StudentFeesSummaryDto  fees,
    List<HomeworkDto>      upcomingHomework,
    List<AnnouncementDto>  announcements
) {}
