package com.sms.api.dto.payroll;

import com.sms.core.enums.ExpenseCategory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ExpenseEntryDto(
    UUID            id,
    ExpenseCategory category,
    String          description,
    BigDecimal      amount,
    LocalDate       expenseDate,
    String          referenceNo,
    String          attachmentUrl,
    String          enteredByName
) {}
