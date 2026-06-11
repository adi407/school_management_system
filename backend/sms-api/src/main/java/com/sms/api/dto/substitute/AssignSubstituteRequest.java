package com.sms.api.dto.substitute;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AssignSubstituteRequest(
    @NotNull UUID assignmentId,
    UUID substituteTeacherId,  // null = mark as SELF_STUDY
    String remarks
) {}
