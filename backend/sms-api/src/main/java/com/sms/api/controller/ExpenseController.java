package com.sms.api.controller;

import com.sms.api.dto.payroll.CreateExpenseRequest;
import com.sms.api.dto.payroll.ExpenseEntryDto;
import com.sms.api.security.UserPrincipal;
import com.sms.api.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/expenses")
@PreAuthorize("isAuthenticated()")
@Tag(name = "Expenses", description = "Manage non-payroll operational expenses for P&L reporting")
public class ExpenseController {

    private final ExpenseService service;

    public ExpenseController(ExpenseService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Log a new operational expense (rent, electricity, etc.)")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','ACCOUNTANT')")
    public ResponseEntity<ExpenseEntryDto> create(
        @Valid @RequestBody CreateExpenseRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(service.create(principal.schoolId(), principal.userId(), request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an expense entry")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','ACCOUNTANT')")
    public ResponseEntity<ExpenseEntryDto> update(
        @PathVariable UUID id,
        @Valid @RequestBody CreateExpenseRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(service.update(principal.schoolId(), id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an expense entry")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','ACCOUNTANT')")
    public ResponseEntity<Void> delete(
        @PathVariable UUID id,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        service.delete(principal.schoolId(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "List expenses for the school in a date range")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','ACCOUNTANT')")
    public ResponseEntity<List<ExpenseEntryDto>> list(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        LocalDate dateFrom = from != null ? from : LocalDate.now().withDayOfMonth(1);
        LocalDate dateTo   = to   != null ? to   : LocalDate.now();
        return ResponseEntity.ok(service.list(principal.schoolId(), dateFrom, dateTo));
    }
}
