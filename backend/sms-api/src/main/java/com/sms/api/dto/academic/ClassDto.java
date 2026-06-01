package com.sms.api.dto.academic;

import java.time.Instant;
import java.util.UUID;

public record ClassDto(
    UUID   id,
    UUID   schoolId,
    String name,
    int    grade,
    String section,
    int    capacity,
    long   studentCount,
    UUID   classTeacherId,
    String classTeacherName,
    Instant createdAt
) {}
