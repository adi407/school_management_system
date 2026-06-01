package com.sms.api.dto.student;

import jakarta.validation.constraints.NotBlank;

public record CreateGuardianRequest(
    @NotBlank String name,
    @NotBlank String relation,
    @NotBlank String phone,
    String email,
    String aadhaarNo,
    String occupation,
    String address,
    boolean isPrimary,
    boolean isAuthorizedPickup
) {}
