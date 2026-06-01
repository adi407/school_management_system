package com.sms.api.dto.announcement;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.List;

public record CreateAnnouncementRequest(
    @NotBlank String       title,
    @NotBlank String       body,
    List<String>           targetRoles,  // null/empty = all roles
    Instant                expiresAt,
    boolean                isPinned
) {}
