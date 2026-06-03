package com.sms.api.entity;

import com.sms.api.entity.base.SchoolScopedEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * Per-staff payslip generated as part of a PayrollRun.
 * Stores every salary component and deduction line item for audit / slip generation.
 */
@Entity
@Table(name = "payslips", indexes = {
    @Index(name = "idx_payslip_school",     columnList = "school_id"),
    @Index(name = "idx_payslip_run",        columnList = "payroll_run_id"),
    @Index(name = "idx_payslip_staff",      columnList = "user_id"),
    @Index(name = "idx_payslip_run_staff",  columnList = "payroll_run_id, user_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uq_payslip_run_staff",
        columnNames = {"payroll_run_id", "user_id"})
})
public class Payslip extends SchoolScopedEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_run_id", nullable = false)
    private PayrollRun payrollRun;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User staff;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salary_structure_id")
    private SalaryStructure salaryStructure;

    // ── Earnings (monthly full-month values) ──────────────────────────────────

    @Column(precision = 12, scale = 2)
    private BigDecimal basicSalary = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal hraAmount = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal daAmount = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal taAmount = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal medicalAllowance = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal otherAllowances = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal grossSalary = BigDecimal.ZERO;       // sum of above

    // ── LOP (Loss of Pay) ─────────────────────────────────────────────────────

    @Column(nullable = false)
    private int totalWorkingDays = 26;

    @Column(nullable = false)
    private int presentDays = 26;

    @Column(nullable = false)
    private int lopDays = 0;

    /** lopDays * (grossSalary / totalWorkingDays) */
    @Column(precision = 12, scale = 2)
    private BigDecimal lopDeduction = BigDecimal.ZERO;

    /** grossSalary after LOP — this is the base for ESI & deductions */
    @Column(precision = 12, scale = 2)
    private BigDecimal effectiveGross = BigDecimal.ZERO;

    // ── Statutory deductions ──────────────────────────────────────────────────

    /** Employee PF: 12% of basic (capped at PF wage ceiling) */
    @Column(precision = 12, scale = 2)
    private BigDecimal pfEmployee = BigDecimal.ZERO;

    /** Employer PF contribution (not deducted from salary, shown for CTC) */
    @Column(precision = 12, scale = 2)
    private BigDecimal pfEmployer = BigDecimal.ZERO;

    /** Employee ESI: 0.75% of effective gross (only if gross ≤ ₹21,000) */
    @Column(precision = 12, scale = 2)
    private BigDecimal esiEmployee = BigDecimal.ZERO;

    /** Employer ESI: 3.25% of effective gross */
    @Column(precision = 12, scale = 2)
    private BigDecimal esiEmployer = BigDecimal.ZERO;

    /** Professional Tax (state slab-based) */
    @Column(precision = 12, scale = 2)
    private BigDecimal professionalTax = BigDecimal.ZERO;

    /** TDS monthly projection */
    @Column(precision = 12, scale = 2)
    private BigDecimal tds = BigDecimal.ZERO;

    /** pfEmployee + esiEmployee + professionalTax + tds */
    @Column(precision = 12, scale = 2)
    private BigDecimal totalDeductions = BigDecimal.ZERO;

    /** effectiveGross - totalDeductions */
    @Column(precision = 12, scale = 2)
    private BigDecimal netSalary = BigDecimal.ZERO;

    // ── Getters / Setters ──────────────────────────────────────────────────────

    public PayrollRun getPayrollRun() { return payrollRun; }
    public void setPayrollRun(PayrollRun payrollRun) { this.payrollRun = payrollRun; }

    public User getStaff() { return staff; }
    public void setStaff(User staff) { this.staff = staff; }

    public SalaryStructure getSalaryStructure() { return salaryStructure; }
    public void setSalaryStructure(SalaryStructure salaryStructure) { this.salaryStructure = salaryStructure; }

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

    public BigDecimal getGrossSalary() { return grossSalary; }
    public void setGrossSalary(BigDecimal grossSalary) { this.grossSalary = grossSalary; }

    public int getTotalWorkingDays() { return totalWorkingDays; }
    public void setTotalWorkingDays(int totalWorkingDays) { this.totalWorkingDays = totalWorkingDays; }

    public int getPresentDays() { return presentDays; }
    public void setPresentDays(int presentDays) { this.presentDays = presentDays; }

    public int getLopDays() { return lopDays; }
    public void setLopDays(int lopDays) { this.lopDays = lopDays; }

    public BigDecimal getLopDeduction() { return lopDeduction; }
    public void setLopDeduction(BigDecimal lopDeduction) { this.lopDeduction = lopDeduction; }

    public BigDecimal getEffectiveGross() { return effectiveGross; }
    public void setEffectiveGross(BigDecimal effectiveGross) { this.effectiveGross = effectiveGross; }

    public BigDecimal getPfEmployee() { return pfEmployee; }
    public void setPfEmployee(BigDecimal pfEmployee) { this.pfEmployee = pfEmployee; }

    public BigDecimal getPfEmployer() { return pfEmployer; }
    public void setPfEmployer(BigDecimal pfEmployer) { this.pfEmployer = pfEmployer; }

    public BigDecimal getEsiEmployee() { return esiEmployee; }
    public void setEsiEmployee(BigDecimal esiEmployee) { this.esiEmployee = esiEmployee; }

    public BigDecimal getEsiEmployer() { return esiEmployer; }
    public void setEsiEmployer(BigDecimal esiEmployer) { this.esiEmployer = esiEmployer; }

    public BigDecimal getProfessionalTax() { return professionalTax; }
    public void setProfessionalTax(BigDecimal professionalTax) { this.professionalTax = professionalTax; }

    public BigDecimal getTds() { return tds; }
    public void setTds(BigDecimal tds) { this.tds = tds; }

    public BigDecimal getTotalDeductions() { return totalDeductions; }
    public void setTotalDeductions(BigDecimal totalDeductions) { this.totalDeductions = totalDeductions; }

    public BigDecimal getNetSalary() { return netSalary; }
    public void setNetSalary(BigDecimal netSalary) { this.netSalary = netSalary; }
}
