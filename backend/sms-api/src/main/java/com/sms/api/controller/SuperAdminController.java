package com.sms.api.controller;

import com.sms.api.dto.feature.FeatureFlagDto;
import com.sms.api.dto.feature.UpdateFeatureFlagsRequest;
import com.sms.api.dto.school.CreateSchoolRequest;
import com.sms.api.dto.school.DeleteSchoolResponse;
import com.sms.api.dto.school.SchoolDto;
import com.sms.api.dto.school.UpdateSchoolRequest;
import com.sms.api.security.UserPrincipal;
import com.sms.api.service.FeatureFlagService;
import com.sms.api.service.SchoolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/super-admin")
@PreAuthorize("hasRole('SUPER_ADMIN')")
@Tag(name = "Super Admin", description = "Platform-level school and feature management")
public class SuperAdminController {

    private final SchoolService schoolService;
    private final FeatureFlagService featureFlagService;

    public SuperAdminController(SchoolService schoolService, FeatureFlagService featureFlagService) {
        this.schoolService      = schoolService;
        this.featureFlagService = featureFlagService;
    }

    @GetMapping("/schools")
    @Operation(summary = "List all schools with filters and pagination")
    public ResponseEntity<Page<SchoolDto>> listSchools(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String tier,
        @RequestParam(required = false) Boolean isActive,
        @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        return ResponseEntity.ok(schoolService.listSchools(search, tier, isActive, pageable));
    }

    @PostMapping("/schools")
    @Operation(summary = "Enroll a new school with an admin account")
    public ResponseEntity<SchoolDto> createSchool(@Valid @RequestBody CreateSchoolRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(schoolService.createSchool(request));
    }

    @GetMapping("/schools/{id}")
    @Operation(summary = "Get school by ID")
    public ResponseEntity<SchoolDto> getSchool(@PathVariable UUID id) {
        return ResponseEntity.ok(schoolService.getSchool(id));
    }

    @PutMapping("/schools/{id}")
    @Operation(summary = "Update school details")
    public ResponseEntity<SchoolDto> updateSchool(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateSchoolRequest request
    ) {
        return ResponseEntity.ok(schoolService.updateSchool(id, request));
    }

    @PatchMapping("/schools/{id}/status")
    @Operation(summary = "Activate or suspend a school")
    public ResponseEntity<Void> setSchoolStatus(
        @PathVariable UUID id,
        @RequestParam boolean active
    ) {
        schoolService.setActive(id, active);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/schools/{id}/soft-delete")
    @Operation(summary = "Soft-delete a school (deactivate school, users, and students)")
    public ResponseEntity<DeleteSchoolResponse> softDeleteSchool(@PathVariable UUID id) {
        return ResponseEntity.ok(schoolService.softDelete(id));
    }

    @DeleteMapping("/schools/{id}")
    @Operation(summary = "Hard-delete a school and all related data permanently")
    public ResponseEntity<DeleteSchoolResponse> hardDeleteSchool(@PathVariable UUID id) {
        return ResponseEntity.ok(schoolService.hardDelete(id));
    }

    @GetMapping("/schools/{id}/features")
    @Operation(summary = "Get feature flags for a school")
    public ResponseEntity<Map<String, FeatureFlagDto>> getSchoolFeatures(@PathVariable UUID id) {
        return ResponseEntity.ok(featureFlagService.getSchoolFlags(id));
    }

    @PutMapping("/schools/{id}/features")
    @Operation(summary = "Bulk update feature flags for a school")
    public ResponseEntity<Void> updateSchoolFeatures(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateFeatureFlagsRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        featureFlagService.updateSchoolFlags(id, request, principal.userId());
        return ResponseEntity.noContent().build();
    }
}
