package com.sms.api.dto.payroll;

import com.sms.core.enums.PayrollStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PayrollRunDto(
    UUID          id,
    int           runMonth,
    int           runYear,
    int           totalWorkingDays,
    PayrollStatus status,

    // Aggregate totals
    BigDecimal totalGross,
    BigDecimal totalPfEmployee,
    BigDecimal totalPfEmployer,
    BigDecimal totalEsiEmployee,
    BigDecimal totalEsiEmployer,
    BigDecimal totalProfessionalTax,
    BigDecimal totalTds,
    BigDecimal totalLopDeduction,
    BigDecimal totalNetPayout,

    // Audit
    String    triggeredByName,
    String    approvedByName,
    Instant   approvedAt,
    Instant   paidAt,
    String    notes,

    int       payslipCount
) {}
