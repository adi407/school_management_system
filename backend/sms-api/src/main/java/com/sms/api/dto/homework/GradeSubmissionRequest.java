package com.sms.api.dto.homework;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Payload a teacher sends when grading a student's submission.
 */
public record GradeSubmissionRequest(
    @NotBlank @Size(max = 5) String grade,    // e.g. "A+", "B", "8/10"
    String remarks                             // optional feedback for student
) {}
