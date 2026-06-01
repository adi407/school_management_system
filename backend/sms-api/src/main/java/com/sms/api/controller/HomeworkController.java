package com.sms.api.controller;

import com.sms.api.dto.homework.CreateHomeworkRequest;
import com.sms.api.dto.homework.HomeworkDto;
import com.sms.api.security.UserPrincipal;
import com.sms.api.service.HomeworkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
@RequestMapping("/api/v1/homework")
@PreAuthorize("isAuthenticated()")
@Tag(name = "Homework", description = "Homework management — create, publish, list, and parent feed")
public class HomeworkController {

    private final HomeworkService homeworkService;

    public HomeworkController(HomeworkService homeworkService) {
        this.homeworkService = homeworkService;
    }

    // ─── Teacher / Admin: paginated list ─────────────────────────────────────

    @GetMapping
    @Operation(summary = "List homework with optional filters (class, subject, date range)")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<Page<HomeworkDto>> list(
        @RequestParam(required = false) UUID      classId,
        @RequestParam(required = false) UUID      subjectId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        @PageableDefault(size = 20, sort = "dueDate") Pageable pageable,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(
            homeworkService.list(principal.schoolId(), classId, subjectId, from, to, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get homework details by ID")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<HomeworkDto> get(
        @PathVariable UUID id,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(homeworkService.get(principal.schoolId(), id));
    }

    @PostMapping
    @Operation(summary = "Create a new homework assignment")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<HomeworkDto> create(
        @Valid @RequestBody CreateHomeworkRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(homeworkService.create(principal.schoolId(), principal.userId(), request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a homework assignment")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<HomeworkDto> update(
        @PathVariable UUID id,
        @Valid @RequestBody CreateHomeworkRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(homeworkService.update(principal.schoolId(), id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a homework assignment")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<Void> delete(
        @PathVariable UUID id,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        homeworkService.delete(principal.schoolId(), id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/publish")
    @Operation(summary = "Publish or unpublish a homework assignment")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<HomeworkDto> setPublished(
        @PathVariable UUID id,
        @RequestParam boolean published,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(homeworkService.setPublished(principal.schoolId(), id, published));
    }

    // ─── Parent / Student feed: upcoming homework for a class ─────────────────
    // (Public within school — parent accesses via their child's classId)

    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming homework for a class — parent / student view")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','TEACHER','PARENT','STUDENT')")
    public ResponseEntity<List<HomeworkDto>> upcoming(
        @RequestParam UUID classId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(homeworkService.getUpcoming(classId));
    }

    @GetMapping("/class/{classId}")
    @Operation(summary = "Get homework for a class in a date range — parent feed")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','TEACHER','PARENT','STUDENT')")
    public ResponseEntity<List<HomeworkDto>> forClass(
        @PathVariable UUID classId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(homeworkService.getForClassInRange(classId, from, to));
    }
}
