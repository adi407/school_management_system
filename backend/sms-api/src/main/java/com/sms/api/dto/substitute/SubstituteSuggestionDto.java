package com.sms.api.dto.substitute;

import java.util.UUID;

/**
 * AI-suggested substitute teacher for a particular period.
 */
public record SubstituteSuggestionDto(
    UUID teacherId,
    String teacherName,
    String department,
    String reason,         // "Free period + teaches Mathematics"
    float confidenceScore, // 0.0 – 1.0
    boolean teachesSameSubject,
    int currentLoadToday   // how many periods already teaching today
) {}
