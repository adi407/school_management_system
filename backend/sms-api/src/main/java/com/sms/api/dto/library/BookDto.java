package com.sms.api.dto.library;

import java.util.UUID;

public record BookDto(
    UUID   id,
    String title,
    String author,
    String isbn,
    String category,
    int    totalCopies,
    int    availableCopies
) {}
