package com.sms.api.dto.registration;

import jakarta.validation.constraints.NotBlank;

public record ApproveRegistrationRequest(
    String subscriptionTier,
    @NotBlank String adminPassword
) {}
