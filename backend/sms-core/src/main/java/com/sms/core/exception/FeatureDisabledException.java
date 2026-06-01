package com.sms.core.exception;

import com.sms.core.enums.FeatureKey;

public class FeatureDisabledException extends RuntimeException {
    private final FeatureKey featureKey;

    public FeatureDisabledException(FeatureKey featureKey) {
        super("Feature '" + featureKey.name() + "' is not enabled for this school. Please upgrade your subscription.");
        this.featureKey = featureKey;
    }

    public FeatureKey getFeatureKey() { return featureKey; }
}
