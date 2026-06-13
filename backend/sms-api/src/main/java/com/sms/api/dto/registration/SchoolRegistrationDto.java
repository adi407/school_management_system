package com.sms.api.dto.registration;

import java.time.Instant;
import java.util.UUID;

public record SchoolRegistrationDto(
    UUID id,
    String schoolName,
    String schoolCode,
    String board,
    String requestedTier,
    String address,
    String city,
    String state,
    String phone,
    String schoolEmail,
    String website,
    Integer studentCount,
    String adminName,
    String adminEmail,
    String adminPhone,
    String adminDesignation,
    String message,
    String status,
    String rejectionReason,
    UUID approvedSchoolId,
    Instant createdAt,
    Instant reviewedAt
) {}
