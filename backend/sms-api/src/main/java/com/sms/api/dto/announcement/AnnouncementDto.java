package com.sms.api.dto.announcement;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AnnouncementDto(
    UUID         id,
    String       title,
    String       body,
    List<String> targetRoles,
    UUID         publishedById,
    String       publishedByName,
    Instant      publishedAt,
    Instant      expiresAt,
    boolean      isPinned
) {}
