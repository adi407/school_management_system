package com.sms.api.dto.school;

import com.sms.core.enums.BoardType;
import com.sms.core.enums.SubscriptionTier;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record SchoolDto(
    UUID id,
    String name,
    String code,
    BoardType board,
    SubscriptionTier subscriptionTier,
    LocalDate subscriptionExpiry,
    String address,
    String phone,
    String email,
    String logoUrl,
    String timezone,
    String locale,
    boolean isActive,
    long studentCount,
    long staffCount,
    Instant createdAt,
    Instant updatedAt
) {}
