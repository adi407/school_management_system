package com.sms.api.dto.library;

import java.time.LocalDate;
import java.util.UUID;

public record BookIssueDto(
    UUID      id,
    UUID      bookId,
    String    bookTitle,
    UUID      studentId,
    String    borrowerName,
    LocalDate issueDate,
    LocalDate dueDate,
    LocalDate returnDate,
    boolean   isReturned,
    boolean   isOverdue
) {}
