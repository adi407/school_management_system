package com.sms.api.dto.academic;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AssignSubjectRequest(
    @NotNull UUID subjectId,
    UUID teacherId          // optional — can assign teacher later
) {}
