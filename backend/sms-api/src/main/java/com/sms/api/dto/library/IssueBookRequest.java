package com.sms.api.dto.library;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record IssueBookRequest(
    @NotNull UUID      bookId,
    UUID               studentId,
    String             borrowerName,
    @NotNull LocalDate issueDate,
    @NotNull LocalDate dueDate
) {}
