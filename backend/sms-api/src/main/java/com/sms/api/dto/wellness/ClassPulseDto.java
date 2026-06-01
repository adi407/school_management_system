package com.sms.api.dto.wellness;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/**
 * Aggregated Campus Pulse result for a class on a given date.
 * Returned to teachers, counselors, and admins.
 */
public record ClassPulseDto(
    UUID      classId,
    String    className,
    LocalDate date,
    int       totalCheckins,
    /** e.g. { "GREAT": 5, "GOOD": 12, "OKAY": 8, "SAD": 3, "STRESSED": 2 } */
    Map<String, Long> moodBreakdown,
    int  positiveCount,   // GREAT + GOOD
    int  neutralCount,    // OKAY
    int  negativeCount,   // SAD + STRESSED
    double negativePct,   // % of total — triggers alert if > 30%
    boolean alertTriggered
) {}
