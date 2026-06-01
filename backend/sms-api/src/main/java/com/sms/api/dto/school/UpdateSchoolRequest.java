package com.sms.api.dto.school;

import com.sms.core.enums.BoardType;
import com.sms.core.enums.SubscriptionTier;
import jakarta.validation.constraints.Email;

import java.time.LocalDate;

public record UpdateSchoolRequest(
    String name,
    BoardType board,
    SubscriptionTier subscriptionTier,
    String address,
    String phone,
    @Email String email,
    String timezone,
    String locale,
    LocalDate subscriptionExpiry,
    Integer maxStudents,
    Integer maxStaff
) {}
