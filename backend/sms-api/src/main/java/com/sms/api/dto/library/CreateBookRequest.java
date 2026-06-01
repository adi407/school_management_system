package com.sms.api.dto.library;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;

public record CreateBookRequest(
    @NotBlank String title,
    String author,
    String isbn,
    String category,
    @Min(1) int totalCopies
) {}
