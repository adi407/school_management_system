package com.sms.api.controller;

import com.sms.api.dto.feature.FeatureFlagDto;
import com.sms.api.security.UserPrincipal;
import com.sms.api.service.FeatureFlagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/features")
@Tag(name = "Feature Flags", description = "Current user's school feature flags — called once after login")
public class FeatureFlagController {

    private final FeatureFlagService featureFlagService;

    public FeatureFlagController(FeatureFlagService featureFlagService) {
        this.featureFlagService = featureFlagService;
    }

    @GetMapping
    @Operation(summary = "Get all feature flags for the current user's school")
    public ResponseEntity<Map<String, FeatureFlagDto>> getMyFlags(
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal.schoolId() == null) {
            // SUPER_ADMIN — return all features enabled
            return ResponseEntity.ok(featureFlagService.getSchoolFlags(null));
        }
        return ResponseEntity.ok(featureFlagService.getSchoolFlags(principal.schoolId()));
    }
}
