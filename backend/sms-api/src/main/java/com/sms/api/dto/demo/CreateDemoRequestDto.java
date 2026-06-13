package com.sms.api.dto.demo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateDemoRequestDto(
    @NotBlank String name,
    @NotBlank @Email String email,
    String phone,
    String schoolName,
    String city,
    String role,
    String message
) {}
