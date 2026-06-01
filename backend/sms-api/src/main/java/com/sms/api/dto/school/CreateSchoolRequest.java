package com.sms.api.dto.school;

import com.sms.core.enums.BoardType;
import com.sms.core.enums.SubscriptionTier;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateSchoolRequest(
    @NotBlank String name,
    @NotBlank String code,
    @NotNull  BoardType board,
    @NotNull  SubscriptionTier subscriptionTier,
    String address,
    String phone,
    @Email String email,
    String timezone,
    String locale,
    LocalDate subscriptionExpiry,
    // Initial admin account
    @NotBlank String adminEmail,
    @NotBlank String adminPassword,
    String adminName
) {}
