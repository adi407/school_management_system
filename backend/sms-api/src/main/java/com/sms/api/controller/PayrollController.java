package com.sms.api.controller;

import com.sms.api.dto.payroll.PayrollRunDto;
import com.sms.api.dto.payroll.PayslipDto;
import com.sms.api.dto.payroll.TriggerPayrollRequest;
import com.sms.api.security.UserPrincipal;
import com.sms.api.security.annotation.RequiresModule;
import com.sms.api.service.PayrollService;
import com.sms.core.enums.StaffModule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payroll")
@PreAuthorize("isAuthenticated()")
@Tag(name = "Payroll", description = "Trigger and manage monthly payroll runs and payslips")
public class PayrollController {

    private final PayrollService service;

    public PayrollController(PayrollService service) {
        this.service = service;
    }

    // ── Runs ──────────────────────────────────────────────────────────────────

    @PostMapping("/runs")
    @Operation(summary = "Trigger a new payroll run — automatically pulls attendance & computes deductions")
    @RequiresModule(value = StaffModule.PAYROLL, permission = "PAYROLL__RUN_PAYROLL")
    public ResponseEntity<PayrollRunDto> trigger(
        @Valid @RequestBody TriggerPayrollRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(service.triggerRun(principal.schoolId(), principal.userId(), request));
    }

    @GetMapping("/runs")
    @Operation(summary = "List all payroll runs for the school (newest first)")
    @RequiresModule(StaffModule.PAYROLL)
    public ResponseEntity<List<PayrollRunDto>> listRuns(
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(service.listRuns(principal.schoolId()));
    }

    @PostMapping("/runs/{runId}/approve")
    @Operation(summary = "Approve a DRAFT payroll run")
    @RequiresModule(value = StaffModule.PAYROLL, permission = "PAYROLL__APPROVE_PAYROLL")
    public ResponseEntity<PayrollRunDto> approve(
        @PathVariable UUID runId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(service.approveRun(principal.schoolId(), runId, principal.userId()));
    }

    @PostMapping("/runs/{runId}/mark-paid")
    @Operation(summary = "Mark an APPROVED run as PAID (salaries disbursed)")
    @RequiresModule(value = StaffModule.PAYROLL, permission = "PAYROLL__MARK_PAID")
    public ResponseEntity<PayrollRunDto> markPaid(
        @PathVariable UUID runId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(service.markPaid(principal.schoolId(), runId));
    }

    // ── Payslips ──────────────────────────────────────────────────────────────

    @GetMapping("/runs/{runId}/payslips")
    @Operation(summary = "Get all payslips for a payroll run")
    @RequiresModule(value = StaffModule.PAYROLL, permission = "PAYROLL__VIEW_PAYSLIPS")
    public ResponseEntity<List<PayslipDto>> getPayslips(
        @PathVariable UUID runId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(service.getPayslipsForRun(principal.schoolId(), runId));
    }

    @GetMapping("/payslips/{payslipId}")
    @Operation(summary = "Get a specific payslip by ID")
    @RequiresModule(value = StaffModule.PAYROLL, permission = "PAYROLL__VIEW_PAYSLIPS")
    public ResponseEntity<PayslipDto> getPayslip(
        @PathVariable UUID payslipId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(service.getPayslip(principal.schoolId(), payslipId));
    }

    /**
     * Self-service: any authenticated staff member can view their own payslips.
     * No @RequiresModule needed — just being logged in is enough.
     */
    @GetMapping("/my-payslips")
    @Operation(summary = "Staff member views their own payslip history")
    public ResponseEntity<List<PayslipDto>> myPayslips(
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(service.getMyPayslips(principal.schoolId(), principal.userId()));
    }
}
