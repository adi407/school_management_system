package com.sms.api.dto.feature;

import com.sms.core.enums.FeatureKey;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record UpdateFeatureFlagsRequest(
    @NotNull Map<FeatureKey, Boolean> flags
) {}
