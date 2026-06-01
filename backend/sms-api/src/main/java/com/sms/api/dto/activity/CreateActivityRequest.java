package com.sms.api.dto.activity;

import jakarta.validation.constraints.NotBlank;

public record CreateActivityRequest(
    @NotBlank String name,
    @NotBlank String category,
    String coach,
    String schedule,
    int    capacity,
    String status
) {}
