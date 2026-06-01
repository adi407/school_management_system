package com.sms.api.controller;

import com.sms.api.dto.activity.ActivityDto;
import com.sms.api.dto.activity.CreateActivityRequest;
import com.sms.api.security.UserPrincipal;
import com.sms.api.service.ActivityService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/activities")
@PreAuthorize("isAuthenticated()")
@Tag(name = "Activities", description = "Co-curricular activity management")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping
    public ResponseEntity<List<ActivityDto>> list(@AuthenticationPrincipal UserPrincipal p) {
        return ResponseEntity.ok(activityService.list(p.schoolId()));
    }

    @PostMapping
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ResponseEntity<ActivityDto> create(@Valid @RequestBody CreateActivityRequest req,
                                               @AuthenticationPrincipal UserPrincipal p) {
        ActivityDto dto = activityService.create(p.schoolId(), req);
        return ResponseEntity.created(URI.create("/api/v1/activities/" + dto.id())).body(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ResponseEntity<ActivityDto> update(@PathVariable UUID id,
                                               @Valid @RequestBody CreateActivityRequest req,
                                               @AuthenticationPrincipal UserPrincipal p) {
        return ResponseEntity.ok(activityService.update(p.schoolId(), id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                        @AuthenticationPrincipal UserPrincipal p) {
        activityService.delete(p.schoolId(), id);
        return ResponseEntity.noContent().build();
    }
}
