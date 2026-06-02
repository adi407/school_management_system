package com.sms.api.dto.auth;

import com.sms.core.enums.FeatureKey;
import com.sms.core.enums.Role;

import java.util.List;
import java.util.UUID;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    UserInfo user
) {
    public record UserInfo(
        UUID id,
        UUID schoolId,
        String email,
        String fullName,
        Role role,
        String profilePhotoUrl,
        List<String> enabledFeatures,
        String schoolName
    ) {}
}
