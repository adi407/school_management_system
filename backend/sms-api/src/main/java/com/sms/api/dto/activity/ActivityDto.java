package com.sms.api.dto.activity;

import java.util.UUID;

public record ActivityDto(
    UUID   id,
    String name,
    String category,
    String coach,
    String schedule,
    int    capacity,
    String status
) {}
