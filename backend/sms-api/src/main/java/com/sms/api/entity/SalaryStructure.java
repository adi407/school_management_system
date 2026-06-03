package com.sms.api.entity;

import com.sms.api.entity.base.SchoolScopedEntity;
import com.sms.core.enums.TaxRegime;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Monthly salary structure for a staff member.
 * Holds salary components (basic, HRA, allowances) and
 * investment declaration fields used to reduce TDS.
 */
@Entity
@Table(name = "salary_structures", indexes = {
    @Index(name = "idx_sal_struct_school", columnList = "school_id"),
    @Index(name = "idx_sal_struct_user",   columnList = "user_id")
})
public class SalaryStructure extends SchoolScopedEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User staff;

    // ── Salary components (monthly, ₹) ────────────────────────────────────────

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal basicSalary = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal hraAmount = BigDecimal.ZERO;           // House Rent Allowance

    @Column(precision = 12, scale = 2)
    private BigDecimal daAmount = BigDecimal.ZERO;            // Dearness Allowance

    @Column(precision = 12, scale = 2)
    private BigDecimal taAmount = BigDecimal.ZERO;            // Travel Allowance

    @Column(precision = 12, scale = 2)
    private BigDecimal medicalAllowance = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal otherAllowances = BigDecimal.ZERO;

    // ── Deduction settings ─────────────────────────────────────────────────────

    /** Whether staff member is enrolled in EPF. If true, 12% of basic (capped at ₹1,800) is deducted. */
    @Column(nullable = false)
    private boolean pfEnrolled = true;

    /** Override the automatically computed PF cap (default: statutory ₹15,000 basic ceiling). */
    @Column(precision = 12, scale = 2)
    private BigDecimal pfWageCeiling = new BigDecimal("15000.00");

    // ── Tax regime & declarations ──────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TaxRegime taxRegime = TaxRegime.NEW;

    /**
     * Annual investment declared under Section 80C (max ₹1,50,000).
     * Only used in OLD regime for TDS reduction.
     */
    @Column(precision = 12, scale = 2)
    private BigDecimal declared80c = BigDecimal.ZERO;

    /**
     * Annual HRA exemption declared by employee.
     * Only used in OLD regime.
     */
    @Column(precision = 12, scale = 2)
    private BigDecimal declaredHraExemption = BigDecimal.ZERO;

    /**
     * Any other annual exemptions declared (NPS, insurance, etc.).
     */
    @Column(precision = 12, scale = 2)
    private BigDecimal declaredOtherExemptions = BigDecimal.ZERO;

    // ── Effective period ───────────────────────────────────────────────────────

    @Column(nullable = false)
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;       // null = currently active

    @Column(nullable = false)
    private boolean isActive = true;

    // ── Getters / setters ──────────────────────────────────────────────────────

    public User getStaff() { return staff; }
    public void setStaff(User staff) { this.staff = staff; }

    public BigDecimal getBasicSalary() { return basicSalary; }
    public void setBasicSalary(BigDecimal basicSalary) { this.basicSalary = basicSalary; }

    public BigDecimal getHraAmount() { return hraAmount; }
    public void setHraAmount(BigDecimal hraAmount) { this.hraAmount = hraAmount; }

    public BigDecimal getDaAmount() { return daAmount; }
    public void setDaAmount(BigDecimal daAmount) { this.daAmount = daAmount; }

    public BigDecimal getTaAmount() { return taAmount; }
    public void setTaAmount(BigDecimal taAmount) { this.taAmount = taAmount; }

    public BigDecimal getMedicalAllowance() { return medicalAllowance; }
    public void setMedicalAllowance(BigDecimal medicalAllowance) { this.medicalAllowance = medicalAllowance; }

    public BigDecimal getOtherAllowances() { return otherAllowances; }
    public void setOtherAllowances(BigDecimal otherAllowances) { this.otherAllowances = otherAllowances; }

    public boolean isPfEnrolled() { return pfEnrolled; }
    public void setPfEnrolled(boolean pfEnrolled) { this.pfEnrolled = pfEnrolled; }

    public BigDecimal getPfWageCeiling() { return pfWageCeiling; }
    public void setPfWageCeiling(BigDecimal pfWageCeiling) { this.pfWageCeiling = pfWageCeiling; }

    public TaxRegime getTaxRegime() { return taxRegime; }
    public void setTaxRegime(TaxRegime taxRegime) { this.taxRegime = taxRegime; }

    public BigDecimal getDeclared80c() { return declared80c; }
    public void setDeclared80c(BigDecimal declared80c) { this.declared80c = declared80c; }

    public BigDecimal getDeclaredHraExemption() { return declaredHraExemption; }
    public void setDeclaredHraExemption(BigDecimal declaredHraExemption) { this.declaredHraExemption = declaredHraExemption; }

    public BigDecimal getDeclaredOtherExemptions() { return declaredOtherExemptions; }
    public void setDeclaredOtherExemptions(BigDecimal declaredOtherExemptions) { this.declaredOtherExemptions = declaredOtherExemptions; }

    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(LocalDate effectiveFrom) { this.effectiveFrom = effectiveFrom; }

    public LocalDate getEffectiveTo() { return effectiveTo; }
    public void setEffectiveTo(LocalDate effectiveTo) { this.effectiveTo = effectiveTo; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    /** Computed gross = basic + HRA + DA + TA + medical + other */
    public BigDecimal computeGross() {
        return basicSalary
            .add(hraAmount == null ? BigDecimal.ZERO : hraAmount)
            .add(daAmount  == null ? BigDecimal.ZERO : daAmount)
            .add(taAmount  == null ? BigDecimal.ZERO : taAmount)
            .add(medicalAllowance == null ? BigDecimal.ZERO : medicalAllowance)
            .add(otherAllowances  == null ? BigDecimal.ZERO : otherAllowances);
    }
}
