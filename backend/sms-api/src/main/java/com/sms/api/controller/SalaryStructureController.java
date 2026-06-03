package com.sms.api.controller;

import com.sms.api.dto.payroll.CreateSalaryStructureRequest;
import com.sms.api.dto.payroll.SalaryStructureDto;
import com.sms.api.security.UserPrincipal;
import com.sms.api.security.annotation.RequiresModule;
import com.sms.api.service.SalaryStructureService;
import com.sms.core.enums.StaffModule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/salary-structures")
@PreAuthorize("isAuthenticated()")
@Tag(name = "Salary Structures", description = "Manage staff salary structures and investment declarations")
public class SalaryStructureController {

    private final SalaryStructureService service;

    public SalaryStructureController(SalaryStructureService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Create a new salary structure for a staff member")
    @RequiresModule(value = StaffModule.PAYROLL, permission = "PAYROLL__MANAGE_SALARY_STRUCTURES")
    public ResponseEntity<SalaryStructureDto> create(
        @Valid @RequestBody CreateSalaryStructureRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(service.create(principal.schoolId(), request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update salary structure (creates a new revision, closes old one)")
    @RequiresModule(value = StaffModule.PAYROLL, permission = "PAYROLL__MANAGE_SALARY_STRUCTURES")
    public ResponseEntity<SalaryStructureDto> update(
        @PathVariable UUID id,
        @Valid @RequestBody CreateSalaryStructureRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(service.update(principal.schoolId(), id, request));
    }

    @GetMapping
    @Operation(summary = "List all salary structures for the school")
    @RequiresModule(value = StaffModule.PAYROLL, permission = "PAYROLL__MANAGE_SALARY_STRUCTURES")
    public ResponseEntity<List<SalaryStructureDto>> listAll(
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(service.listBySchool(principal.schoolId()));
    }

    @GetMapping("/staff/{staffId}")
    @Operation(summary = "List salary structure history for a staff member")
    @RequiresModule(value = StaffModule.PAYROLL, permission = "PAYROLL__MANAGE_SALARY_STRUCTURES")
    public ResponseEntity<List<SalaryStructureDto>> listForStaff(
        @PathVariable UUID staffId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(service.listByStaff(principal.schoolId(), staffId));
    }

    /**
     * Self-service: any staff member can update investment declarations on their own structure.
     * No @RequiresModule — just being authenticated is enough.
     */
    @PatchMapping("/{id}/declarations")
    @Operation(summary = "Update investment declarations (80C, HRA, other) for a salary structure")
    public ResponseEntity<SalaryStructureDto> updateDeclarations(
        @PathVariable UUID id,
        @RequestParam(required = false) BigDecimal declared80c,
        @RequestParam(required = false) BigDecimal declaredHra,
        @RequestParam(required = false) BigDecimal declaredOther,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(
            service.updateDeclarations(principal.schoolId(), id, declared80c, declaredHra, declaredOther));
    }
}
