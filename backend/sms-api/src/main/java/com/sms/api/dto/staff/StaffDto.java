package com.sms.api.dto.staff;

import java.time.Instant;
import java.util.UUID;

public record StaffDto(
    UUID    id,
    String  email,
    String  firstName,
    String  lastName,
    String  fullName,
    String  phone,
    String  department,
    String  role,
    boolean isActive,
    Instant createdAt
) {}
