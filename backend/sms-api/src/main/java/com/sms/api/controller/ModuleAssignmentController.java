package com.sms.api.controller;

import com.sms.api.dto.module.AssignModuleRequest;
import com.sms.api.dto.module.MyModuleDto;
import com.sms.api.dto.module.StaffModuleAssignmentDto;
import com.sms.api.security.UserPrincipal;
import com.sms.api.security.annotation.RequiresModule;
import com.sms.api.service.ModuleAssignmentService;
import com.sms.core.enums.StaffModule;
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
@RequestMapping("/api/v1")
@PreAuthorize("isAuthenticated()")
@Tag(name = "Module Assignment", description = "Assign and manage functional modules for staff members")
public class ModuleAssignmentController {

    private final ModuleAssignmentService service;

    public ModuleAssignmentController(ModuleAssignmentService service) {
        this.service = service;
    }

    // ── Self-service (any authenticated user) ─────────────────────────────────

    /**
     * GET /api/v1/my-modules
     * Returns the calling user's active module assignments.
     * Angular calls this at login and caches the result to drive the sidebar.
     */
    @GetMapping("/my-modules")
    @Operation(summary = "Get modules assigned to the calling user")
    public ResponseEntity<List<MyModuleDto>> getMyModules(
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(
            service.getMyModules(principal.userId(), principal.schoolId()));
    }

    // ── Admin endpoints (SCHOOL_SETUP module required) ────────────────────────

    /**
     * GET /api/v1/modules/school
     * All active module assignments in the school — for the admin overview table.
     */
    @GetMapping("/modules/school")
    @Operation(summary = "List all active module assignments in the school")
    @RequiresModule(value = StaffModule.SCHOOL_SETUP,
                    permission = "SCHOOL_SETUP__ASSIGN_MODULES")
    public ResponseEntity<List<StaffModuleAssignmentDto>> getSchoolAssignments(
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(service.getAllSchoolAssignments(principal.schoolId()));
    }

    /**
     * GET /api/v1/staff/{staffId}/modules
     * All assignments (active + revoked) for a specific staff member.
     */
    @GetMapping("/staff/{staffId}/modules")
    @Operation(summary = "Get all module assignments for a staff member")
    @RequiresModule(value = StaffModule.SCHOOL_SETUP,
                    permission = "SCHOOL_SETUP__ASSIGN_MODULES")
    public ResponseEntity<List<StaffModuleAssignmentDto>> getStaffModules(
        @PathVariable UUID staffId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(service.getStaffModules(principal.schoolId(), staffId));
    }

    /**
     * POST /api/v1/staff/{staffId}/modules
     * Assign (or update) a module for a staff member.
     * Idempotent — re-posting re-activates a previously revoked assignment.
     */
    @PostMapping("/staff/{staffId}/modules")
    @Operation(summary = "Assign a module to a staff member")
    @RequiresModule(value = StaffModule.SCHOOL_SETUP,
                    permission = "SCHOOL_SETUP__ASSIGN_MODULES")
    public ResponseEntity<StaffModuleAssignmentDto> assignModule(
        @PathVariable UUID staffId,
        @Valid @RequestBody AssignModuleRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(
            service.assignModule(principal.schoolId(), staffId, principal.userId(), request));
    }

    /**
     * DELETE /api/v1/staff/{staffId}/modules/{module}
     * Revoke a module from a staff member (soft-delete).
     */
    @DeleteMapping("/staff/{staffId}/modules/{module}")
    @Operation(summary = "Revoke a module from a staff member")
    @RequiresModule(value = StaffModule.SCHOOL_SETUP,
                    permission = "SCHOOL_SETUP__ASSIGN_MODULES")
    public ResponseEntity<Void> revokeModule(
        @PathVariable UUID staffId,
        @PathVariable StaffModule module,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        service.revokeModule(principal.schoolId(), staffId, module);
        return ResponseEntity.noContent().build();
    }
}
