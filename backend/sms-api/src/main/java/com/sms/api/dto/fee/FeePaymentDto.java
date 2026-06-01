package com.sms.api.dto.fee;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record FeePaymentDto(
    UUID       id,
    UUID       studentId,
    String     studentName,
    String     admissionNo,
    UUID       feeStructureId,
    String     feeType,
    BigDecimal amountPaid,
    LocalDate  paymentDate,
    String     paymentMode,
    String     receiptNo,
    String     remarks,
    UUID       collectedById,
    String     collectedByEmail
) {}
