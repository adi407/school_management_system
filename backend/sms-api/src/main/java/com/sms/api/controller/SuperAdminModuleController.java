package com.sms.api.controller;

import com.sms.api.dto.module.AssignModuleRequest;
import com.sms.api.dto.module.StaffModuleAssignmentDto;
import com.sms.api.dto.school.SchoolUserDto;
import com.sms.api.entity.User;
import com.sms.api.repository.UserRepository;
import com.sms.api.service.ModuleAssignmentService;
import com.sms.core.enums.StaffModule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Platform-level module assignment management.
 * Lets the SUPER_ADMIN view and control which modules any user in any school can access.
 *
 * All endpoints are under /api/v1/super-admin/schools/{schoolId}/staff/...
 * and are locked down to SUPER_ADMIN role only.
 */
@RestController
@RequestMapping("/api/v1/super-admin/schools/{schoolId}/staff")
@PreAuthorize("hasRole('SUPER_ADMIN')")
@Tag(name = "Super Admin – Module Assignment",
     description = "Platform-level management of school-user module assignments")
public class SuperAdminModuleController {

    private final ModuleAssignmentService moduleService;
    private final UserRepository          userRepository;

    public SuperAdminModuleController(ModuleAssignmentService moduleService,
                                      UserRepository userRepository) {
        this.moduleService  = moduleService;
        this.userRepository = userRepository;
    }

    // ── User listing ──────────────────────────────────────────────────────────

    /**
     * GET /api/v1/super-admin/schools/{schoolId}/staff
     * Returns every active user belonging to the given school so the super-admin
     * can pick one and manage their module assignments.
     */
    @GetMapping
    @Operation(summary = "List all users in a school")
    public ResponseEntity<List<SchoolUserDto>> listSchoolUsers(@PathVariable UUID schoolId) {
        List<SchoolUserDto> users = userRepository.findAllBySchoolId(schoolId)
            .stream()
            .filter(User::isActive)
            .map(this::toSchoolUserDto)
            .toList();
        return ResponseEntity.ok(users);
    }

    // ── Module management ─────────────────────────────────────────────────────

    /**
     * GET /api/v1/super-admin/schools/{schoolId}/staff/{userId}/modules
     * All module assignments (active + revoked) for a specific school user.
     */
    @GetMapping("/{userId}/modules")
    @Operation(summary = "Get module assignments for a school user")
    public ResponseEntity<List<StaffModuleAssignmentDto>> getUserModules(
        @PathVariable UUID schoolId,
        @PathVariable UUID userId
    ) {
        return ResponseEntity.ok(moduleService.getStaffModules(schoolId, userId));
    }

    /**
     * POST /api/v1/super-admin/schools/{schoolId}/staff/{userId}/modules
     * Assign or update a module for a school user (idempotent).
     */
    @PostMapping("/{userId}/modules")
    @Operation(summary = "Assign or update a module for a school user")
    public ResponseEntity<StaffModuleAssignmentDto> assignModule(
        @PathVariable UUID schoolId,
        @PathVariable UUID userId,
        @Valid @RequestBody AssignModuleRequest request
    ) {
        return ResponseEntity.ok(
            moduleService.assignModuleForSchool(schoolId, userId, request));
    }

    /**
     * DELETE /api/v1/super-admin/schools/{schoolId}/staff/{userId}/modules/{module}
     * Revoke a module from a school user (soft-delete).
     */
    @DeleteMapping("/{userId}/modules/{module}")
    @Operation(summary = "Revoke a module from a school user")
    public ResponseEntity<Void> revokeModule(
        @PathVariable UUID schoolId,
        @PathVariable UUID userId,
        @PathVariable StaffModule module
    ) {
        moduleService.revokeModule(schoolId, userId, module);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/v1/super-admin/schools/{schoolId}/staff/{userId}/modules/grant-all
     * Convenience endpoint: grant all 12 modules with full access in one call.
     * Useful for resetting a user's access back to the default admin state.
     */
    @PostMapping("/{userId}/modules/grant-all")
    @Operation(summary = "Grant all modules (full access) to a school user")
    public ResponseEntity<Void> grantAllModules(
        @PathVariable UUID schoolId,
        @PathVariable UUID userId
    ) {
        moduleService.assignAllModules(schoolId, userId);
        return ResponseEntity.noContent().build();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private SchoolUserDto toSchoolUserDto(User u) {
        String first = u.getFirstName() != null ? u.getFirstName() : "";
        String last  = u.getLastName()  != null ? u.getLastName()  : "";
        String full  = (first + " " + last).trim();
        return new SchoolUserDto(
            u.getId(),
            full.isEmpty() ? u.getEmail() : full,
            u.getEmail(),
            u.getRole().name(),
            u.isActive()
        );
    }
}
