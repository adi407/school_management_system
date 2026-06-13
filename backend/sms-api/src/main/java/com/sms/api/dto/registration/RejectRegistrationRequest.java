package com.sms.api.dto.registration;

import jakarta.validation.constraints.NotBlank;

public record RejectRegistrationRequest(
    @NotBlank String reason
) {}
