package com.sms.api.controller;

import com.sms.api.dto.payroll.MarkStaffAttendanceRequest;
import com.sms.api.dto.payroll.StaffAttendanceDto;
import com.sms.api.security.UserPrincipal;
import com.sms.api.security.annotation.RequiresModule;
import com.sms.api.service.StaffAttendanceService;
import com.sms.core.enums.StaffModule;
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
@RequiresModule(value = StaffModule.HR, permission = "HR__MARK_STAFF_ATTENDANCE")
@Tag(name = "Staff Attendance", description = "Track daily staff attendance for LOP payroll deduction")
public class StaffAttendanceController {

    private final StaffAttendanceService service;

    public StaffAttendanceController(StaffAttendanceService service) {
        this.service = service;
    }

    @PostMapping("/mark")
    @Operation(summary = "Mark attendance for multiple staff members on a date")
    public ResponseEntity<List<StaffAttendanceDto>> mark(
        @Valid @RequestBody MarkStaffAttendanceRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(
            service.markAttendance(principal.schoolId(), principal.userId(), request));
    }

    @GetMapping("/day")
    @Operation(summary = "Get school-wide staff attendance roll for a specific date")
    public ResponseEntity<List<StaffAttendanceDto>> getDayRoll(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(service.getDayRoll(principal.schoolId(), date));
    }

    @GetMapping("/staff/{staffId}/history")
    @Operation(summary = "Get attendance history for a specific staff member")
    public ResponseEntity<List<StaffAttendanceDto>> getHistory(
        @PathVariable UUID staffId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(service.getStaffHistory(principal.schoolId(), staffId, from, to));
    }
}
