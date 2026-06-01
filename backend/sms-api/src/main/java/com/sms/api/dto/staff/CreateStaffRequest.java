package com.sms.api.dto.staff;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateStaffRequest(
    @NotBlank @Email String email,
    @NotBlank String  firstName,
    @NotBlank String  lastName,
    String            phone,
    String            department,
    @NotNull  String  role,       // TEACHER | ACCOUNTANT | LIBRARIAN | TRANSPORT_MANAGER
    String            password    // null → generate default
) {}
