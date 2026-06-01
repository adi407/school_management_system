package com.sms.api.controller;

import com.sms.api.dto.staff.CreateStaffRequest;
import com.sms.api.dto.staff.StaffDto;
import com.sms.api.dto.staff.UpdateStaffRequest;
import com.sms.api.security.UserPrincipal;
import com.sms.api.service.StaffService;
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
@RequestMapping("/api/v1/staff")
@PreAuthorize("isAuthenticated()")
@Tag(name = "Staff", description = "Staff management for school administrators")
public class StaffController {

    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    @GetMapping
    @Operation(summary = "List all staff members for the school")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN')")
    public ResponseEntity<List<StaffDto>> list(
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(staffService.listStaff(principal.schoolId()));
    }

    @PostMapping
    @Operation(summary = "Create a new staff member account")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ResponseEntity<StaffDto> create(
        @Valid @RequestBody CreateStaffRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(staffService.createStaff(principal.schoolId(), request));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update staff member details or status")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ResponseEntity<StaffDto> update(
        @PathVariable UUID id,
        @RequestBody UpdateStaffRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(staffService.updateStaff(principal.schoolId(), id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate a staff member")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ResponseEntity<Void> deactivate(
        @PathVariable UUID id,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        staffService.deleteStaff(principal.schoolId(), id);
        return ResponseEntity.noContent().build();
    }
}
