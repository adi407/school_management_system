package com.sms.api.config;

import com.sms.api.annotation.RequiresFeature;
import com.sms.api.repository.FeatureFlagRepository;
import com.sms.api.security.UserPrincipal;
import com.sms.core.exception.FeatureDisabledException;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * AOP aspect that intercepts @RequiresFeature-annotated methods.
 * Extracts schoolId from the JWT-derived SecurityContext and checks
 * the feature_flags table. Throws FeatureDisabledException (→ HTTP 403)
 * if the feature is not enabled, keeping feature-gating out of business logic.
 */
@Aspect
@Component
public class FeatureFlagAspect {

    private final FeatureFlagRepository featureFlagRepository;

    public FeatureFlagAspect(FeatureFlagRepository featureFlagRepository) {
        this.featureFlagRepository = featureFlagRepository;
    }

    @Before("@annotation(requiresFeature)")
    public void checkFeature(RequiresFeature requiresFeature) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            return; // let SecurityConfig handle unauthenticated
        }

        if (principal.schoolId() == null) {
            return; // SUPER_ADMIN has no schoolId — always allowed
        }

        boolean enabled = featureFlagRepository
            .findBySchoolIdAndFeatureKey(principal.schoolId(), requiresFeature.value())
            .map(ff -> ff.isEnabled())
            .orElse(false);

        if (!enabled) {
            throw new FeatureDisabledException(requiresFeature.value());
        }
    }
}
