package com.sms.api.dto.academic;

import jakarta.validation.constraints.NotBlank;

public record CreateSubjectRequest(
    @NotBlank String name,
    @NotBlank String code,
    String type,        // CORE | ELECTIVE | ACTIVITY | LANGUAGE  (default CORE)
    Integer creditHours
) {}
