package com.sms.api.dto.school;

import java.util.UUID;

/**
 * Lightweight user record returned by the super-admin school-user listing endpoint.
 * Used on the "Admin Modules" page so the super admin can pick a user and manage their modules.
 */
public record SchoolUserDto(
    UUID    id,
    String  fullName,
    String  email,
    String  role,
    boolean isActive
) {}
