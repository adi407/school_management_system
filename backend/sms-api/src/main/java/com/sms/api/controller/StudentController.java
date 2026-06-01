package com.sms.api.controller;

import com.sms.api.dto.student.*;
import com.sms.api.security.UserPrincipal;
import com.sms.api.service.StudentService;
import com.sms.core.enums.Gender;
import com.sms.core.enums.StudentCategory;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/students")
@PreAuthorize("isAuthenticated()")
@Tag(name = "Students", description = "Student CRUD and guardian management")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    // ── Student endpoints ─────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "List students with pagination and optional filters")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','TEACHER','LIBRARIAN')")
    public ResponseEntity<Page<StudentSummaryDto>> listStudents(
        @RequestParam(required = false) UUID    classId,
        @RequestParam(required = false) Gender  gender,
        @RequestParam(required = false) StudentCategory category,
        @RequestParam(required = false) Boolean isActive,
        @RequestParam(required = false) String  search,
        @PageableDefault(size = 20, sort = "lastName") Pageable pageable,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(studentService.listStudents(
            principal.schoolId(), classId, gender, category, isActive, search, pageable));
    }

    @PostMapping
    @Operation(summary = "Enroll a new student")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ResponseEntity<StudentDto> createStudent(
        @Valid @RequestBody CreateStudentRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(studentService.createStudent(principal.schoolId(), request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get full student profile by ID")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<StudentDto> getStudent(
        @PathVariable UUID id,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(studentService.getStudent(principal.schoolId(), id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update student details (partial — only non-null fields applied)")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ResponseEntity<StudentDto> updateStudent(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateStudentRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(studentService.updateStudent(principal.schoolId(), id, request));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Activate or deactivate a student")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ResponseEntity<Void> setStudentStatus(
        @PathVariable UUID id,
        @RequestParam boolean active,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        studentService.setActive(principal.schoolId(), id, active);
        return ResponseEntity.noContent().build();
    }

    // ── Guardian sub-resource ─────────────────────────────────────────────────

    @GetMapping("/{studentId}/guardians")
    @Operation(summary = "List guardians for a student")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<List<GuardianDto>> getGuardians(
        @PathVariable UUID studentId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(studentService.getGuardians(principal.schoolId(), studentId));
    }

    @PostMapping("/{studentId}/guardians")
    @Operation(summary = "Add a guardian to a student")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ResponseEntity<GuardianDto> addGuardian(
        @PathVariable UUID studentId,
        @Valid @RequestBody CreateGuardianRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(studentService.addGuardian(principal.schoolId(), studentId, request));
    }

    @PutMapping("/{studentId}/guardians/{guardianId}")
    @Operation(summary = "Update a guardian")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ResponseEntity<GuardianDto> updateGuardian(
        @PathVariable UUID studentId,
        @PathVariable UUID guardianId,
        @Valid @RequestBody CreateGuardianRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(
            studentService.updateGuardian(principal.schoolId(), studentId, guardianId, request));
    }

    @DeleteMapping("/{studentId}/guardians/{guardianId}")
    @Operation(summary = "Remove a guardian")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ResponseEntity<Void> deleteGuardian(
        @PathVariable UUID studentId,
        @PathVariable UUID guardianId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        studentService.deleteGuardian(principal.schoolId(), studentId, guardianId);
        return ResponseEntity.noContent().build();
    }
}
