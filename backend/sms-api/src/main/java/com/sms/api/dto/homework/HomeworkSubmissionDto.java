package com.sms.api.dto.homework;

import java.time.Instant;
import java.util.UUID;

/**
 * Full view of a single homework submission.
 * Returned to teachers (full detail) and students (own submission only).
 */
public record HomeworkSubmissionDto(
    UUID    id,
    UUID    homeworkId,
    String  homeworkTitle,
    UUID    studentId,
    String  studentName,
    String  admissionNo,
    UUID    schoolId,
    /** PENDING | SUBMITTED | LATE | GRADED */
    String  status,
    String  content,
    String  attachments,
    String  remarks,
    String  grade,
    UUID    gradedById,
    String  gradedByEmail,
    Instant submittedAt,
    Instant gradedAt,
    Instant createdAt
) {}
