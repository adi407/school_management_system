package com.sms.api.controller;

import com.sms.api.dto.ptm.*;
import com.sms.api.security.UserPrincipal;
import com.sms.api.service.PtmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ptm")
@PreAuthorize("hasAnyRole('SCHOOL_ADMIN','TEACHER')")
@Tag(name = "PTM Prep AI", description = "AI-powered Parent-Teacher Meeting preparation")
public class PtmController {

    private final PtmService ptmService;

    public PtmController(PtmService ptmService) {
        this.ptmService = ptmService;
    }

    @PostMapping("/meetings")
    @Operation(summary = "Schedule a new PTM meeting")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ResponseEntity<PtmMeetingDto> createMeeting(
        @Valid @RequestBody CreatePtmRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(
            ptmService.createMeeting(principal.schoolId(), request, principal.userId()));
    }

    @GetMapping("/meetings")
    @Operation(summary = "List all PTM meetings for the school")
    public ResponseEntity<List<PtmMeetingDto>> listMeetings(
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ptmService.listMeetings(principal.schoolId()));
    }

    @GetMapping("/meetings/{meetingId}")
    @Operation(summary = "Get PTM meeting details")
    public ResponseEntity<PtmMeetingDto> getMeeting(
        @PathVariable UUID meetingId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ptmService.getMeeting(principal.schoolId(), meetingId));
    }

    @PostMapping("/meetings/{meetingId}/generate-briefings")
    @Operation(summary = "AI-generate briefings for all students in the class")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<List<PtmBriefingDto>> generateBriefings(
        @PathVariable UUID meetingId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ptmService.generateBriefings(principal.schoolId(), meetingId));
    }

    @GetMapping("/meetings/{meetingId}/briefings")
    @Operation(summary = "Get all briefings for a PTM meeting")
    public ResponseEntity<List<PtmBriefingDto>> getBriefings(
        @PathVariable UUID meetingId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ptmService.getBriefings(principal.schoolId(), meetingId));
    }

    @PostMapping("/briefings/{briefingId}/review")
    @Operation(summary = "Mark a briefing as reviewed by teacher")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<PtmBriefingDto> reviewBriefing(
        @PathVariable UUID briefingId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ptmService.reviewBriefing(briefingId, principal.userId()));
    }
}
