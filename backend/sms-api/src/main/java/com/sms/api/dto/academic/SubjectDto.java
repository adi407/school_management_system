package com.sms.api.dto.academic;

import java.time.Instant;
import java.util.UUID;

public record SubjectDto(
    UUID   id,
    UUID   schoolId,
    String name,
    String code,
    String type,
    Integer creditHours,
    Instant createdAt
) {}
