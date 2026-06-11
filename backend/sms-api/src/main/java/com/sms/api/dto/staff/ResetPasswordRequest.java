package com.sms.api.dto.staff;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for super-admin password reset on behalf of a school user.
 */
public record ResetPasswordRequest(

    @NotBlank(message = "New password must not be blank")
    @Size(min = 8, message = "Password must be at least 8 characters")
    String newPassword

) {}
