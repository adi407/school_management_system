package com.sms.api.controller;

import com.sms.api.dto.timetable.TimetableSlotDto;
import com.sms.api.dto.timetable.UpsertSlotRequest;
import com.sms.api.security.UserPrincipal;
import com.sms.api.service.TimetableService;
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
@RequestMapping("/api/v1/timetable")
@PreAuthorize("isAuthenticated()")
@Tag(name = "Timetable", description = "Class schedule management")
public class TimetableController {

    private final TimetableService timetableService;

    public TimetableController(TimetableService timetableService) {
        this.timetableService = timetableService;
    }

    @GetMapping("/class/{classId}")
    @Operation(summary = "Get full weekly timetable for a class")
    public ResponseEntity<List<TimetableSlotDto>> getClassTimetable(
        @PathVariable UUID classId,
        @RequestParam UUID academicYearId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(
            timetableService.getClassTimetable(principal.schoolId(), classId, academicYearId));
    }

    @GetMapping("/teacher/{teacherId}")
    @Operation(summary = "Get weekly timetable for a teacher")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<List<TimetableSlotDto>> getTeacherTimetable(
        @PathVariable UUID teacherId,
        @RequestParam UUID academicYearId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(
            timetableService.getTeacherTimetable(principal.schoolId(), teacherId, academicYearId));
    }

    @PutMapping("/slot")
    @Operation(summary = "Create or update a timetable slot (upsert by class+day+period)")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ResponseEntity<TimetableSlotDto> upsertSlot(
        @Valid @RequestBody UpsertSlotRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(timetableService.upsertSlot(principal.schoolId(), request));
    }

    @DeleteMapping("/slot/{slotId}")
    @Operation(summary = "Delete a timetable slot")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ResponseEntity<Void> deleteSlot(
        @PathVariable UUID slotId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        timetableService.deleteSlot(principal.schoolId(), slotId);
        return ResponseEntity.noContent().build();
    }
}
