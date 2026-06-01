package com.sms.api.controller;

import com.sms.api.dto.fee.*;
import com.sms.api.security.UserPrincipal;
import com.sms.api.service.FeeService;
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
@RequestMapping("/api/v1/fees")
@PreAuthorize("isAuthenticated()")
@Tag(name = "Fees", description = "Fee structures and payment management")
public class FeeController {

    private final FeeService feeService;

    public FeeController(FeeService feeService) {
        this.feeService = feeService;
    }

    // ── Fee Structures ────────────────────────────────────────────────────────

    @GetMapping("/structures")
    @Operation(summary = "List all fee structures for the school")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','ACCOUNTANT','TEACHER')")
    public ResponseEntity<List<FeeStructureDto>> listStructures(
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(feeService.listStructures(principal.schoolId()));
    }

    @PostMapping("/structures")
    @Operation(summary = "Create a new fee structure")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','ACCOUNTANT')")
    public ResponseEntity<FeeStructureDto> createStructure(
        @Valid @RequestBody CreateFeeStructureRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(feeService.createStructure(principal.schoolId(), request));
    }

    @DeleteMapping("/structures/{id}")
    @Operation(summary = "Delete a fee structure")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','ACCOUNTANT')")
    public ResponseEntity<Void> deleteStructure(
        @PathVariable UUID id,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        feeService.deleteStructure(principal.schoolId(), id);
        return ResponseEntity.noContent().build();
    }

    // ── Payments ──────────────────────────────────────────────────────────────

    @PostMapping("/pay")
    @Operation(summary = "Record a fee payment for a student")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','ACCOUNTANT')")
    public ResponseEntity<FeePaymentDto> recordPayment(
        @Valid @RequestBody RecordPaymentRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(feeService.recordPayment(principal.schoolId(), principal.userId(), request));
    }

    @GetMapping("/student/{studentId}/history")
    @Operation(summary = "Get payment history for a student")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','ACCOUNTANT','PARENT','STUDENT')")
    public ResponseEntity<List<FeePaymentDto>> getHistory(
        @PathVariable UUID studentId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(feeService.getPaymentHistory(principal.schoolId(), studentId));
    }

    @GetMapping("/student/{studentId}/summary")
    @Operation(summary = "Get full fees summary (structures, paid, balance) for a student")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','ACCOUNTANT','PARENT','STUDENT')")
    public ResponseEntity<StudentFeesSummaryDto> getSummary(
        @PathVariable UUID studentId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(feeService.getStudentFeesSummary(principal.schoolId(), studentId));
    }
}
