package com.sms.api.dto.payroll;

import com.sms.core.enums.ExpenseCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateExpenseRequest(
    @NotNull ExpenseCategory category,
    @NotBlank String description,
    @NotNull @DecimalMin("0.01") BigDecimal amount,
    @NotNull LocalDate expenseDate,
    String referenceNo,
    String attachmentUrl
) {}
