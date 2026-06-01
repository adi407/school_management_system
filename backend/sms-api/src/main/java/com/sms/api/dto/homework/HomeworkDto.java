package com.sms.api.dto.homework;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record HomeworkDto(
    UUID      id,
    UUID      schoolId,
    UUID      classId,
    String    className,
    UUID      subjectId,
    String    subjectName,
    UUID      teacherId,
    String    teacherEmail,
    String    title,
    String    description,
    LocalDate dueDate,
    Integer   estimatedMinutes,
    String    attachments,
    boolean   isPublished,
    boolean   isSchoolWide,
    /** Days until due — negative means overdue */
    long      daysUntilDue,
    Instant   createdAt,
    Instant   updatedAt
) {}
