package com.sms.api.dto.fee;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RecordPaymentRequest(
    @NotNull UUID       studentId,
    UUID                feeStructureId,   // optional reference
    @NotNull @DecimalMin("0.01") BigDecimal amountPaid,
    @NotNull LocalDate  paymentDate,
    @NotBlank String    paymentMode,      // CASH | ONLINE | CHEQUE | DD | UPI
    String              remarks
) {}
