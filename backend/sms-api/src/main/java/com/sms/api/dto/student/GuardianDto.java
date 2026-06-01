package com.sms.api.dto.student;

import java.util.UUID;

public record GuardianDto(
    UUID id,
    String name,
    String relation,
    String phone,
    String email,
    String aadhaarNo,
    String occupation,
    String address,
    boolean isPrimary,
    boolean isAuthorizedPickup,
    String photoUrl
) {}
