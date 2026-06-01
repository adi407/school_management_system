package com.sms.api.controller;

import com.sms.api.dto.academic.CreateSubjectRequest;
import com.sms.api.dto.academic.SubjectDto;
import com.sms.api.security.UserPrincipal;
import com.sms.api.service.SubjectService;
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
@RequestMapping("/api/v1/subjects")
@PreAuthorize("isAuthenticated()")
@Tag(name = "Subjects", description = "Manage school subjects")
public class SubjectController {

    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @GetMapping
    @Operation(summary = "List all subjects for the current school")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','TEACHER','LIBRARIAN')")
    public ResponseEntity<List<SubjectDto>> list(
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(subjectService.list(principal.schoolId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a subject by ID")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','TEACHER','LIBRARIAN')")
    public ResponseEntity<SubjectDto> get(
        @PathVariable UUID id,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(subjectService.get(principal.schoolId(), id));
    }

    @PostMapping
    @Operation(summary = "Create a new subject")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ResponseEntity<SubjectDto> create(
        @Valid @RequestBody CreateSubjectRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(subjectService.create(principal.schoolId(), request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a subject")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ResponseEntity<SubjectDto> update(
        @PathVariable UUID id,
        @Valid @RequestBody CreateSubjectRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(subjectService.update(principal.schoolId(), id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a subject (only if no homework assigned)")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ResponseEntity<Void> delete(
        @PathVariable UUID id,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        subjectService.delete(principal.schoolId(), id);
        return ResponseEntity.noContent().build();
    }
}
