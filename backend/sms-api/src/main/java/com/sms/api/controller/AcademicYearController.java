package com.sms.api.controller;

import com.sms.api.dto.academic.AcademicYearDto;
import com.sms.api.dto.academic.CreateAcademicYearRequest;
import com.sms.api.security.UserPrincipal;
import com.sms.api.service.AcademicYearService;
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
@RequestMapping("/api/v1/academic-years")
@PreAuthorize("isAuthenticated()")
@Tag(name = "Academic Years", description = "Manage academic years for the school")
public class AcademicYearController {

    private final AcademicYearService academicYearService;

    public AcademicYearController(AcademicYearService academicYearService) {
        this.academicYearService = academicYearService;
    }

    @GetMapping
    @Operation(summary = "List all academic years for the current school")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','TEACHER','LIBRARIAN')")
    public ResponseEntity<List<AcademicYearDto>> list(
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(academicYearService.list(principal.schoolId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single academic year by ID")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','TEACHER','LIBRARIAN')")
    public ResponseEntity<AcademicYearDto> get(
        @PathVariable UUID id,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(academicYearService.get(principal.schoolId(), id));
    }

    @PostMapping
    @Operation(summary = "Create a new academic year")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ResponseEntity<AcademicYearDto> create(
        @Valid @RequestBody CreateAcademicYearRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(academicYearService.create(principal.schoolId(), request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an academic year")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ResponseEntity<AcademicYearDto> update(
        @PathVariable UUID id,
        @Valid @RequestBody CreateAcademicYearRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(academicYearService.update(principal.schoolId(), id, request));
    }
}
