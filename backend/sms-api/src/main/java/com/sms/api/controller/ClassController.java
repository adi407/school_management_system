package com.sms.api.controller;

import com.sms.api.dto.academic.AssignSubjectRequest;
import com.sms.api.dto.academic.ClassDto;
import com.sms.api.dto.academic.ClassSubjectDto;
import com.sms.api.dto.academic.CreateClassRequest;
import com.sms.api.security.UserPrincipal;
import com.sms.api.service.ClassService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/classes")
@PreAuthorize("isAuthenticated()")
@Tag(name = "Classes", description = "Manage school classes / sections")
public class ClassController {

    private final ClassService classService;

    public ClassController(ClassService classService) {
        this.classService = classService;
    }

    @GetMapping
    @Operation(summary = "List all classes for the current school")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','TEACHER','LIBRARIAN')")
    public ResponseEntity<List<ClassDto>> list(
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(classService.list(principal.schoolId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single class by ID")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','TEACHER','LIBRARIAN')")
    public ResponseEntity<ClassDto> get(
        @PathVariable UUID id,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(classService.get(principal.schoolId(), id));
    }

    @PostMapping
    @Operation(summary = "Create a new class / section")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ResponseEntity<ClassDto> create(
        @Valid @RequestBody CreateClassRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(classService.create(principal.schoolId(), request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a class (name, capacity, teacher)")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ResponseEntity<ClassDto> update(
        @PathVariable UUID id,
        @Valid @RequestBody CreateClassRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(classService.update(principal.schoolId(), id, request));
    }

    // ── Subject Assignment ────────────────────────────────────────────────────

    @GetMapping("/{classId}/subjects")
    @Operation(summary = "List subjects assigned to a class")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<List<ClassSubjectDto>> listSubjects(
        @PathVariable UUID classId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(classService.listSubjects(principal.schoolId(), classId));
    }

    @PostMapping("/{classId}/subjects")
    @Operation(summary = "Assign a subject (+ optional teacher) to a class")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ResponseEntity<ClassSubjectDto> assignSubject(
        @PathVariable UUID classId,
        @Valid @RequestBody AssignSubjectRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(classService.assignSubject(principal.schoolId(), classId, request));
    }

    @PatchMapping("/{classId}/subjects/{cstId}/teacher")
    @Operation(summary = "Update the teacher for an assigned subject")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ResponseEntity<ClassSubjectDto> updateTeacher(
        @PathVariable UUID classId,
        @PathVariable UUID cstId,
        @RequestBody Map<String, UUID> body,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(
            classService.updateSubjectTeacher(principal.schoolId(), classId, cstId, body.get("teacherId"))
        );
    }

    @DeleteMapping("/{classId}/subjects/{cstId}")
    @Operation(summary = "Remove a subject from a class")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ResponseEntity<Void> removeSubject(
        @PathVariable UUID classId,
        @PathVariable UUID cstId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        classService.removeSubject(principal.schoolId(), classId, cstId);
        return ResponseEntity.noContent().build();
    }
}
