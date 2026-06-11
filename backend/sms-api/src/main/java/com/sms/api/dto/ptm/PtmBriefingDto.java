package com.sms.api.dto.ptm;

import java.util.UUID;

public record PtmBriefingDto(
    UUID id,
    UUID ptmMeetingId,
    UUID studentId,
    String studentName,
    String className,
    UUID teacherId,
    String teacherName,

    // Stats
    Float attendancePct,
    Float avgMarks,
    Float homeworkCompletionPct,
    String wellnessTrend,

    // AI content
    String aiSummary,
    String talkingPoints,
    String parentPreview,

    String status
) {}
