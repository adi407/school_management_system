package com.sms.api.entity;

import com.sms.api.entity.base.SchoolScopedEntity;
import com.sms.core.enums.PayrollStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * A monthly payroll run.  Workflow: DRAFT → APPROVED → PAID.
 * One run per school per month; payslips are children of this entity.
 */
@Entity
@Table(name = "payroll_runs", indexes = {
    @Index(name = "idx_payroll_run_school", columnList = "school_id"),
    @Index(name = "idx_payroll_run_month",  columnList = "school_id, run_month, run_year")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uq_payroll_run_month",
        columnNames = {"school_id", "run_month", "run_year"})
})
public class PayrollRun extends SchoolScopedEntity {

    /** Calendar month (1-12) for which salaries are being paid */
    @Column(name = "run_month", nullable = false)
    private int runMonth;

    /** Calendar year, e.g. 2025 */
    @Column(name = "run_year", nullable = false)
    private int runYear;

    /** Total working days declared for this month (school can set this) */
    @Column(nullable = false)
    private int totalWorkingDays = 26;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private PayrollStatus status = PayrollStatus.DRAFT;

    // ── Aggregate totals (populated after run) ────────────────────────────────

    @Column(precision = 14, scale = 2)
    private BigDecimal totalGross = BigDecimal.ZERO;

    @Column(precision = 14, scale = 2)
    private BigDecimal totalPfEmployee = BigDecimal.ZERO;

    @Column(precision = 14, scale = 2)
    private BigDecimal totalPfEmployer = BigDecimal.ZERO;

    @Column(precision = 14, scale = 2)
    private BigDecimal totalEsiEmployee = BigDecimal.ZERO;

    @Column(precision = 14, scale = 2)
    private BigDecimal totalEsiEmployer = BigDecimal.ZERO;

    @Column(precision = 14, scale = 2)
    private BigDecimal totalProfessionalTax = BigDecimal.ZERO;

    @Column(precision = 14, scale = 2)
    private BigDecimal totalTds = BigDecimal.ZERO;

    @Column(precision = 14, scale = 2)
    private BigDecimal totalLopDeduction = BigDecimal.ZERO;

    @Column(precision = 14, scale = 2)
    private BigDecimal totalNetPayout = BigDecimal.ZERO;

    // ── Audit ─────────────────────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "triggered_by")
    private User triggeredBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    private Instant approvedAt;

    private Instant paidAt;

    @Column(length = 1000)
    private String notes;

    // ── Getters / Setters ──────────────────────────────────────────────────────

    public int getRunMonth() { return runMonth; }
    public void setRunMonth(int runMonth) { this.runMonth = runMonth; }

    public int getRunYear() { return runYear; }
    public void setRunYear(int runYear) { this.runYear = runYear; }

    public int getTotalWorkingDays() { return totalWorkingDays; }
    public void setTotalWorkingDays(int totalWorkingDays) { this.totalWorkingDays = totalWorkingDays; }

    public PayrollStatus getStatus() { return status; }
    public void setStatus(PayrollStatus status) { this.status = status; }

    public BigDecimal getTotalGross() { return totalGross; }
    public void setTotalGross(BigDecimal totalGross) { this.totalGross = totalGross; }

    public BigDecimal getTotalPfEmployee() { return totalPfEmployee; }
    public void setTotalPfEmployee(BigDecimal totalPfEmployee) { this.totalPfEmployee = totalPfEmployee; }

    public BigDecimal getTotalPfEmployer() { return totalPfEmployer; }
    public void setTotalPfEmployer(BigDecimal totalPfEmployer) { this.totalPfEmployer = totalPfEmployer; }

    public BigDecimal getTotalEsiEmployee() { return totalEsiEmployee; }
    public void setTotalEsiEmployee(BigDecimal totalEsiEmployee) { this.totalEsiEmployee = totalEsiEmployee; }

    public BigDecimal getTotalEsiEmployer() { return totalEsiEmployer; }
    public void setTotalEsiEmployer(BigDecimal totalEsiEmployer) { this.totalEsiEmployer = totalEsiEmployer; }

    public BigDecimal getTotalProfessionalTax() { return totalProfessionalTax; }
    public void setTotalProfessionalTax(BigDecimal totalProfessionalTax) { this.totalProfessionalTax = totalProfessionalTax; }

    public BigDecimal getTotalTds() { return totalTds; }
    public void setTotalTds(BigDecimal totalTds) { this.totalTds = totalTds; }

    public BigDecimal getTotalLopDeduction() { return totalLopDeduction; }
    public void setTotalLopDeduction(BigDecimal totalLopDeduction) { this.totalLopDeduction = totalLopDeduction; }

    public BigDecimal getTotalNetPayout() { return totalNetPayout; }
    public void setTotalNetPayout(BigDecimal totalNetPayout) { this.totalNetPayout = totalNetPayout; }

    public User getTriggeredBy() { return triggeredBy; }
    public void setTriggeredBy(User triggeredBy) { this.triggeredBy = triggeredBy; }

    public User getApprovedBy() { return approvedBy; }
    public void setApprovedBy(User approvedBy) { this.approvedBy = approvedBy; }

    public Instant getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Instant approvedAt) { this.approvedAt = approvedAt; }

    public Instant getPaidAt() { return paidAt; }
    public void setPaidAt(Instant paidAt) { this.paidAt = paidAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
