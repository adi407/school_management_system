package com.sms.api.dto.registration;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SchoolRegistrationRequest(
    @NotBlank @Size(max = 200) String schoolName,
    @NotBlank @Size(min = 3, max = 20) String schoolCode,
    @NotBlank String board,
    String requestedTier,
    String address,
    String city,
    String state,
    String phone,
    @Email String schoolEmail,
    String website,
    Integer studentCount,
    @NotBlank @Size(max = 200) String adminName,
    @NotBlank @Email String adminEmail,
    String adminPhone,
    String adminDesignation,
    @Size(max = 1000) String message
) {}
