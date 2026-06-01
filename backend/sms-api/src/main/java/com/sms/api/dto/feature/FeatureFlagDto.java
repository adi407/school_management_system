package com.sms.api.dto.feature;

import com.sms.core.enums.FeatureKey;

import java.time.Instant;

public record FeatureFlagDto(
    FeatureKey featureKey,
    boolean isEnabled,
    String config,
    Instant updatedAt
) {}
