package com.sms.api.controller;

import com.sms.api.dto.substitute.*;
import com.sms.api.security.UserPrincipal;
import com.sms.api.service.SubstituteService;
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

@RestController
@RequestMapping("/api/v1/substitutes")
@PreAuthorize("hasAnyRole('SCHOOL_ADMIN','TEACHER')")
@Tag(name = "Smart Substitute", description = "AI-powered teacher substitute management")
public class SubstituteController {

    private final SubstituteService substituteService;

    public SubstituteController(SubstituteService substituteService) {
        this.substituteService = substituteService;
    }

    @PostMapping("/report-absence")
    @Operation(summary = "Report a teacher absence — auto-generates substitute suggestions")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ResponseEntity<List<SubstituteAssignmentDto>> reportAbsence(
        @Valid @RequestBody ReportAbsenceRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(
            substituteService.reportAbsence(principal.schoolId(), request, principal.userId()));
    }

    @GetMapping("/date/{date}")
    @Operation(summary = "Get all substitute assignments for a date")
    public ResponseEntity<List<SubstituteAssignmentDto>> getByDate(
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(substituteService.getByDate(principal.schoolId(), date));
    }

    @GetMapping("/pending/{date}")
    @Operation(summary = "Get pending substitute assignments for admin review")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ResponseEntity<List<SubstituteAssignmentDto>> getPending(
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(substituteService.getPending(principal.schoolId(), date));
    }

    @GetMapping("/{assignmentId}/suggestions")
    @Operation(summary = "Get ranked substitute suggestions for an assignment")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ResponseEntity<List<SubstituteSuggestionDto>> getSuggestions(
        @PathVariable UUID assignmentId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(substituteService.getSuggestions(principal.schoolId(), assignmentId));
    }

    @PostMapping("/assign")
    @Operation(summary = "Confirm a substitute assignment or mark as self-study")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ResponseEntity<SubstituteAssignmentDto> assignSubstitute(
        @Valid @RequestBody AssignSubstituteRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(
            substituteService.assignSubstitute(principal.schoolId(), request, principal.userId()));
    }
}
