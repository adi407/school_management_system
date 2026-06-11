package com.sms.api.dto.substitute;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record ReportAbsenceRequest(
    @NotNull UUID teacherId,
    @NotNull LocalDate absenceDate,
    String remarks
) {}
