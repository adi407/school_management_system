package com.sms.api.dto.staff;

public record UpdateStaffRequest(
    String  firstName,
    String  lastName,
    String  phone,
    String  department,
    String  role,
    Boolean isActive
) {}
