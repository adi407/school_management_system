package com.sms.api.controller;

import com.sms.api.dto.attendance.AttendanceRecordDto;
import com.sms.api.dto.attendance.AttendanceSummaryDto;
import com.sms.api.dto.attendance.MarkAttendanceRequest;
import com.sms.api.security.UserPrincipal;
import com.sms.api.security.annotation.RequiresModule;
import com.sms.api.service.AttendanceService;
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
@RequestMapping("/api/v1/attendance")
@PreAuthorize("isAuthenticated()")
@Tag(name = "Attendance", description = "Mark and view student attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    // ── Mark bulk attendance for a class (teacher / admin) ───────────────────

    @PostMapping("/mark")
    @Operation(summary = "Mark attendance for an entire class on a date")
    @RequiresModule(value = StaffModule.TEACHING, permission = "TEACHING__MARK_ATTENDANCE")
    public ResponseEntity<List<AttendanceRecordDto>> mark(
        @Valid @RequestBody MarkAttendanceRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(
            attendanceService.markAttendance(principal.schoolId(), principal.userId(), request));
    }

    // ── Get roll for a class on a date ───────────────────────────────────────

    @GetMapping("/class/{classId}")
    @Operation(summary = "Get attendance roll for a class on a specific date")
    @RequiresModule(value = StaffModule.TEACHING, permission = "TEACHING__MARK_ATTENDANCE")
    public ResponseEntity<List<AttendanceRecordDto>> getClassRoll(
        @PathVariable UUID classId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(
            attendanceService.getClassRoll(principal.schoolId(), classId, date));
    }

    // ── Student's own history ─────────────────────────────────────────────────

    @GetMapping("/student/{studentId}/history")
    @Operation(summary = "Get attendance history for a student in a date range")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','TEACHER','STUDENT','PARENT')")
    public ResponseEntity<List<AttendanceRecordDto>> getHistory(
        @PathVariable UUID studentId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(
            attendanceService.getStudentHistory(principal.schoolId(), studentId, from, to));
    }

    // ── Attendance summary / percentage ───────────────────────────────────────

    @GetMapping("/student/{studentId}/summary")
    @Operation(summary = "Get attendance percentage summary for a student")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','TEACHER','STUDENT','PARENT')")
    public ResponseEntity<AttendanceSummaryDto> getSummary(
        @PathVariable UUID studentId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        LocalDate dateFrom = from != null ? from : LocalDate.now().withDayOfMonth(1);
        LocalDate dateTo   = to   != null ? to   : LocalDate.now();
        return ResponseEntity.ok(
            attendanceService.getStudentSummary(principal.schoolId(), studentId, dateFrom, dateTo));
    }
}
