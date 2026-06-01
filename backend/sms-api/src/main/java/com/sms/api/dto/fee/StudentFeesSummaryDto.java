package com.sms.api.dto.fee;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record StudentFeesSummaryDto(
    UUID        studentId,
    String      studentName,
    BigDecimal  totalFees,
    BigDecimal  totalPaid,
    BigDecimal  balance,
    List<FeeStructureDto>  structures,
    List<FeePaymentDto>    payments
) {}
