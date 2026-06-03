package com.sms.api.security.annotation;

import com.sms.core.enums.StaffModule;

import java.lang.annotation.*;

/**
 * Placed on controller methods (or classes) to enforce module-level access control.
 *
 * <p>The {@link com.sms.api.security.ModuleAuthorizationAspect} intercepts calls,
 * looks up the authenticated user's assignments, and throws 403 if the check fails.
 *
 * <p>Examples:
 * <pre>
 * // Requires the HR module (any sub-permission)
 * {@literal @}RequiresModule(StaffModule.HR)
 *
 * // Requires the PAYROLL module AND the RUN_PAYROLL sub-permission
 * {@literal @}RequiresModule(value = StaffModule.PAYROLL, permission = "PAYROLL__RUN_PAYROLL")
 * </pre>
 *
 * <p>SUPER_ADMIN bypasses all module checks automatically.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresModule {

    /** The functional module required to call this endpoint. */
    StaffModule value();

    /**
     * Optional fine-grained sub-permission (a {@link com.sms.core.enums.ModulePermission} name).
     * If empty (default), only module-level access is required.
     */
    String permission() default "";
}
