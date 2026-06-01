package com.sms.api.controller;

import com.sms.api.dto.announcement.AnnouncementDto;
import com.sms.api.dto.announcement.CreateAnnouncementRequest;
import com.sms.api.security.UserPrincipal;
import com.sms.api.service.AnnouncementService;
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
@RequestMapping("/api/v1/announcements")
@PreAuthorize("isAuthenticated()")
@Tag(name = "Announcements", description = "School notice board")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @GetMapping
    @Operation(summary = "List all active announcements (filtered by caller's role)")
    public ResponseEntity<List<AnnouncementDto>> list(
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(announcementService.listActive(principal.schoolId()));
    }

    @GetMapping("/all")
    @Operation(summary = "List all announcements including expired (admin only)")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ResponseEntity<List<AnnouncementDto>> listAll(
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(announcementService.list(principal.schoolId()));
    }

    @PostMapping
    @Operation(summary = "Create a new announcement")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<AnnouncementDto> create(
        @Valid @RequestBody CreateAnnouncementRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(announcementService.create(principal.schoolId(), principal.userId(), request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an announcement")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<AnnouncementDto> update(
        @PathVariable UUID id,
        @Valid @RequestBody CreateAnnouncementRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(
            announcementService.update(principal.schoolId(), id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an announcement")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ResponseEntity<Void> delete(
        @PathVariable UUID id,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        announcementService.delete(principal.schoolId(), id);
        return ResponseEntity.noContent().build();
    }
}
