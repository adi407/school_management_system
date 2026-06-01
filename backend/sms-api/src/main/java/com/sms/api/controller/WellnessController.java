package com.sms.api.controller;

import com.sms.api.dto.wellness.ClassPulseDto;
import com.sms.api.dto.wellness.WellnessCheckinRequest;
import com.sms.api.security.UserPrincipal;
import com.sms.api.service.WellnessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Campus Pulse — anonymous daily mood check-in.
 *
 * POST  /api/v1/wellness/checkin          → Student submits mood
 * GET   /api/v1/wellness/pulse/class/{id} → Teacher / counselor: class pulse
 * GET   /api/v1/wellness/pulse/school     → Admin / counselor: all classes today
 * GET   /api/v1/wellness/trend/{classId}  → Mood trend over a date range
 */
@RestController
@RequestMapping("/api/v1/wellness")
@PreAuthorize("isAuthenticated()")
@Tag(name = "Campus Pulse", description = "Daily student wellness check-in and mood analytics")
public class WellnessController {

    private final WellnessService wellnessService;

    public WellnessController(WellnessService wellnessService) {
        this.wellnessService = wellnessService;
    }

    /**
     * Student / Parent submits today's mood.
     * One check-in per student per day (idempotent for anonymous mode).
     */
    @PostMapping("/checkin")
    @Operation(summary = "Submit a daily mood check-in (one per student per day)")
    @PreAuthorize("hasAnyRole('STUDENT','PARENT','TEACHER','SCHOOL_ADMIN')")
    public ResponseEntity<Void> checkin(
        @Valid @RequestBody WellnessCheckinRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        wellnessService.checkin(principal.schoolId(), principal.userId(), request);
        return ResponseEntity.accepted().build();
    }

    /**
     * Teacher / counselor: aggregated mood data for a single class today (or given date).
     * Includes alert flag when ≥30% of check-ins are negative.
     */
    @GetMapping("/pulse/class/{classId}")
    @Operation(summary = "Get Campus Pulse for a specific class on a date")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<ClassPulseDto> classPulse(
        @PathVariable UUID classId,
        @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        return ResponseEntity.ok(
            wellnessService.getClassPulse(principal.schoolId(), classId, targetDate));
    }

    /**
     * Admin / counselor: school-wide pulse — one entry per class, today (or given date).
     * Perfect for the dashboard overview card.
     */
    @GetMapping("/pulse/school")
    @Operation(summary = "Get Campus Pulse for all classes in the school on a date")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<List<ClassPulseDto>> schoolPulse(
        @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        return ResponseEntity.ok(
            wellnessService.getSchoolPulse(principal.schoolId(), targetDate));
    }

    /**
     * Counselor / admin: mood trend for a class over a date range.
     * Returns one ClassPulseDto per day that had check-ins, newest first.
     */
    @GetMapping("/trend/{classId}")
    @Operation(summary = "Get mood trend for a class over a date range")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<List<ClassPulseDto>> trend(
        @PathVariable UUID classId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(
            wellnessService.getClassTrend(principal.schoolId(), classId, from, to));
    }
}
