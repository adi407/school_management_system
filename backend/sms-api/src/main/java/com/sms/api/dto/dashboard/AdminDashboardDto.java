package com.sms.api.dto.dashboard;

import java.math.BigDecimal;
import java.util.List;

public record AdminDashboardDto(
    long       totalStudents,
    long       activeStudents,
    double     attendanceToday,
    BigDecimal feeCollectedToday,
    BigDecimal totalFeePending,
    long       staffCount,
    long       upcomingExams,
    long       booksOverdue,
    List<ActivityItem> recentActivity
) {
    public record ActivityItem(String text, String type, String time) {}
}
