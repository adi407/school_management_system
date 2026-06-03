package com.sms.api.controller;

import com.sms.api.dto.payroll.ProfitLossReportDto;
import com.sms.api.security.UserPrincipal;
import com.sms.api.security.annotation.RequiresModule;
import com.sms.api.service.FinanceReportService;
import com.sms.core.enums.StaffModule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/finance/reports")
@PreAuthorize("isAuthenticated()")
@RequiresModule(value = StaffModule.ACCOUNTING, permission = "ACCOUNTING__VIEW_REPORTS")
@Tag(name = "Finance Reports", description = "P&L and financial reporting for school administrators")
public class FinanceReportController {

    private final FinanceReportService service;

    public FinanceReportController(FinanceReportService service) {
        this.service = service;
    }

    /**
     * GET /api/v1/finance/reports/pl?year=2025&fromMonth=4&toMonth=3
     * Returns a full P&L for the requested year/month range.
     */
    @GetMapping("/pl")
    @Operation(summary = "Generate Profit & Loss report for a month range within a year")
    public ResponseEntity<ProfitLossReportDto> getProfitLoss(
        @RequestParam int year,
        @RequestParam(defaultValue = "1")  int fromMonth,
        @RequestParam(defaultValue = "12") int toMonth,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (fromMonth < 1 || fromMonth > 12 || toMonth < 1 || toMonth > 12 || fromMonth > toMonth) {
            throw new IllegalArgumentException("fromMonth and toMonth must be 1-12 with fromMonth ≤ toMonth");
        }
        return ResponseEntity.ok(
            service.generateReport(principal.schoolId(), year, fromMonth, toMonth));
    }
}
