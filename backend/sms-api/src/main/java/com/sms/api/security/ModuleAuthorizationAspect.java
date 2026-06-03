package com.sms.api.security;

import com.sms.api.security.annotation.RequiresModule;
import com.sms.api.service.ModuleAssignmentService;
import com.sms.core.enums.Role;
import com.sms.core.enums.StaffModule;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * AOP aspect that enforces {@link RequiresModule} on controller methods and classes.
 *
 * <p>Two advices are used to avoid ambiguous binding when both class- and method-level
 * annotations are present:
 * <ol>
 *   <li>Method-level {@code @RequiresModule} always wins when present.</li>
 *   <li>Class-level {@code @RequiresModule} applies to all methods that do NOT
 *       themselves carry the annotation.</li>
 * </ol>
 *
 * <p>SUPER_ADMIN bypasses all module checks.
 */
@Aspect
@Component
public class ModuleAuthorizationAspect {

    private final ModuleAssignmentService moduleService;

    public ModuleAuthorizationAspect(ModuleAssignmentService moduleService) {
        this.moduleService = moduleService;
    }

    // ── 1. Method-level @RequiresModule ────────────────────────────────────────

    @Before("@annotation(requiresModule)")
    public void checkMethodAnnotation(RequiresModule requiresModule) {
        enforce(requiresModule);
    }

    // ── 2. Class-level @RequiresModule (only when method has NO annotation) ────

    @Before("@within(requiresModule) && !@annotation(com.sms.api.security.annotation.RequiresModule)")
    public void checkClassAnnotation(RequiresModule requiresModule) {
        enforce(requiresModule);
    }

    // ── Shared enforcement logic ───────────────────────────────────────────────

    private void enforce(RequiresModule requiresModule) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw new AccessDeniedException("Not authenticated");
        }

        // SUPER_ADMIN bypasses all module checks
        if (principal.role() == Role.SUPER_ADMIN) {
            return;
        }

        StaffModule module     = requiresModule.value();
        String      permission = requiresModule.permission();

        boolean granted;
        if (permission.isBlank()) {
            granted = moduleService.hasModule(principal.userId(), principal.schoolId(), module);
        } else {
            granted = moduleService.hasPermission(
                principal.userId(), principal.schoolId(), module, permission);
        }

        if (!granted) {
            String msg = permission.isBlank()
                ? "Access to module [" + module + "] is not assigned to your account"
                : "Permission [" + permission + "] is not granted for module [" + module + "]";
            throw new AccessDeniedException(msg);
        }
    }
}
