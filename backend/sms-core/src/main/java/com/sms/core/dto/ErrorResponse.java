package com.sms.core.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    Instant timestamp,
    int status,
    String code,
    String message,
    List<FieldError> errors,
    Boolean upgradeRequired,
    String featureKey
) {
    public record FieldError(String field, String message) {}

    public static ErrorResponse of(int status, String code, String message) {
        return new ErrorResponse(Instant.now(), status, code, message, null, null, null);
    }

    public static ErrorResponse ofValidation(int status, String message, List<FieldError> errors) {
        return new ErrorResponse(Instant.now(), status, "VALIDATION_ERROR", message, errors, null, null);
    }

    public static ErrorResponse ofFeature(String featureKey) {
        return new ErrorResponse(Instant.now(), 403, "FEATURE_DISABLED",
            "Feature '" + featureKey + "' is not enabled. Please upgrade your subscription.",
            null, true, featureKey);
    }
}
