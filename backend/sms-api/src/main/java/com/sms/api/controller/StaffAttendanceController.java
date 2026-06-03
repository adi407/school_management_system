package com.sms.api.controller;

import com.sms.api.dto.payroll.MarkStaffAttendanceRequest;
import com.sms.api.dto.payroll.StaffAttendanceDto;
import com.sms.api.security.UserPrincipal;
import com.sms.api.service.StaffAttendanceService;
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
@RequestMapping("/api/v1/staff-attendance")
@PreAuthorize("isAuthenticated()")
@Tag(name = "Staff Attendance", description = "Track daily staff attendance for LOP payroll deduction")
public class StaffAttendanceController {

    private final StaffAttendanceService service;

    public StaffAttendanceController(StaffAttendanceService service) {
        this.service = service;
    }

    @PostMapping("/mark")
    @Operation(summary = "Mark attendance for multiple staff members on a date")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','ACCOUNTANT')")
    public ResponseEntity<List<StaffAttendanceDto>> mark(
        @Valid @RequestBody MarkStaffAttendanceRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(
            service.markAttendance(principal.schoolId(), principal.userId(), request));
    }

    @GetMapping("/day")
    @Operation(summary = "Get school-wide staff attendance roll for a specific date")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','ACCOUNTANT')")
    public ResponseEntity<List<StaffAttendanceDto>> getDayRoll(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(service.getDayRoll(principal.schoolId(), date));
    }

    @GetMapping("/staff/{staffId}/history")
    @Operation(summary = "Get attendance history for a specific staff member")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','ACCOUNTANT')")
    public ResponseEntity<List<StaffAttendanceDto>> getHistory(
        @PathVariable UUID staffId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(service.getStaffHistory(principal.schoolId(), staffId, from, to));
    }
}
