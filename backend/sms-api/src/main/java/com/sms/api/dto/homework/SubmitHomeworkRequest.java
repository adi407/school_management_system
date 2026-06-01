package com.sms.api.dto.homework;

import jakarta.validation.constraints.NotBlank;

/**
 * Payload a student sends when submitting homework.
 * Attachments is an optional JSON array of file URLs (uploaded separately via S3 pre-sign).
 */
public record SubmitHomeworkRequest(
    @NotBlank String content,      // written answer / link / description
    String attachments             // JSON array of attachment URLs, e.g. ["https://..."]
) {}
