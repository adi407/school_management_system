package com.sms.api.service;

import com.sms.api.entity.SalaryStructure;
import com.sms.core.enums.TaxRegime;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Month;

/**
 * Stateless calculation engine for Indian statutory payroll deductions.
 *
 * <p>Covers:
 * <ul>
 *   <li>PF   — 12% of basic (employee + employer), capped at ₹15,000 basic ceiling</li>
 *   <li>ESI  — 0.75% employee / 3.25% employer on gross (only if gross ≤ ₹21,000)</li>
 *   <li>PT   — Professional Tax per generic monthly-gross slab (₹0/₹175/₹200/₹300 Feb)</li>
 *   <li>TDS  — Annual projection method, old or new tax regime with declaration deductions</li>
 *   <li>LOP  — Proportional gross deduction for absent days</li>
 * </ul>
 */
@Component
public class PayrollCalculationEngine {

    // ── Constants ──────────────────────────────────────────────────────────────

    private static final BigDecimal PF_RATE          = new BigDecimal("0.12");
    private static final BigDecimal ESI_EE_RATE      = new BigDecimal("0.0075");
    private static final BigDecimal ESI_ER_RATE      = new BigDecimal("0.0325");
    private static final BigDecimal ESI_GROSS_LIMIT  = new BigDecimal("21000");

    // Old-regime standard deduction (annual)
    private static final BigDecimal OLD_STANDARD_DEDUCTION = new BigDecimal("50000");
    // New-regime standard deduction (annual, FY 2024-25 onwards)
    private static final BigDecimal NEW_STANDARD_DEDUCTION = new BigDecimal("75000");
    // 80C cap
    private static final BigDecimal SECTION_80C_LIMIT      = new BigDecimal("150000");

    // ── Public result record ───────────────────────────────────────────────────

    public record DeductionResult(
        BigDecimal pfEmployee,
        BigDecimal pfEmployer,
        BigDecimal esiEmployee,
        BigDecimal esiEmployer,
        BigDecimal professionalTax,
        BigDecimal tds,
        BigDecimal totalEmployeeDeductions   // pfEmployee + esiEmployee + pt + tds
    ) {}

    // ── LOP Calculation ────────────────────────────────────────────────────────

    /**
     * Computes loss-of-pay deduction.
     *
     * @param grossSalary      full-month gross
     * @param totalWorkingDays total working days in the month (set by school, e.g. 26)
     * @param presentDays      days the staff actually attended (PRESENT + HALF_DAY*0.5)
     * @return proportional deduction amount (rounded to 2 decimal places)
     */
    public BigDecimal computeLopDeduction(BigDecimal grossSalary,
                                          int totalWorkingDays,
                                          BigDecimal presentDays) {
        if (totalWorkingDays <= 0 || grossSalary.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal lopDays = BigDecimal.valueOf(totalWorkingDays).subtract(presentDays);
        if (lopDays.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;

        BigDecimal dailyRate = grossSalary.divide(BigDecimal.valueOf(totalWorkingDays), 4, RoundingMode.HALF_UP);
        return dailyRate.multiply(lopDays).setScale(2, RoundingMode.HALF_UP);
    }

    // ── Main deduction calculation ─────────────────────────────────────────────

    /**
     * Calculates all statutory deductions for a single staff member.
     *
     * @param structure       the active salary structure
     * @param effectiveGross  gross after LOP (base for ESI/PF/PT/TDS)
     * @param payMonth        1-12, needed for PT February slab
     * @param payYear         calendar year, needed for TDS regime boundary (future use)
     */
    public DeductionResult calculate(SalaryStructure structure,
                                     BigDecimal effectiveGross,
                                     int payMonth,
                                     int payYear) {

        BigDecimal basic = structure.getBasicSalary();

        BigDecimal pfEmployee = computePfEmployee(structure, basic);
        BigDecimal pfEmployer = computePfEmployer(structure, basic);
        BigDecimal esiEmployee = computeEsiEmployee(effectiveGross);
        BigDecimal esiEmployer = computeEsiEmployer(effectiveGross);
        BigDecimal pt = computeProfessionalTax(effectiveGross, payMonth);
        BigDecimal tds = computeTds(structure, effectiveGross);

        BigDecimal totalEeDeductions = pfEmployee
            .add(esiEmployee)
            .add(pt)
            .add(tds);

        return new DeductionResult(pfEmployee, pfEmployer, esiEmployee, esiEmployer, pt, tds, totalEeDeductions);
    }

    // ── PF ─────────────────────────────────────────────────────────────────────

    private BigDecimal computePfEmployee(SalaryStructure s, BigDecimal basic) {
        if (!s.isPfEnrolled()) return BigDecimal.ZERO;
        BigDecimal ceiling = s.getPfWageCeiling() != null ? s.getPfWageCeiling() : new BigDecimal("15000");
        BigDecimal pfBase  = basic.min(ceiling);
        return pfBase.multiply(PF_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal computePfEmployer(SalaryStructure s, BigDecimal basic) {
        if (!s.isPfEnrolled()) return BigDecimal.ZERO;
        BigDecimal ceiling = s.getPfWageCeiling() != null ? s.getPfWageCeiling() : new BigDecimal("15000");
        BigDecimal pfBase  = basic.min(ceiling);
        return pfBase.multiply(PF_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    // ── ESI ────────────────────────────────────────────────────────────────────

    private BigDecimal computeEsiEmployee(BigDecimal effectiveGross) {
        if (effectiveGross.compareTo(ESI_GROSS_LIMIT) > 0) return BigDecimal.ZERO;
        return effectiveGross.multiply(ESI_EE_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal computeEsiEmployer(BigDecimal effectiveGross) {
        if (effectiveGross.compareTo(ESI_GROSS_LIMIT) > 0) return BigDecimal.ZERO;
        return effectiveGross.multiply(ESI_ER_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    // ── Professional Tax ───────────────────────────────────────────────────────

    /**
     * Generic slab applicable in most Indian states (Maharashtra / Karnataka common slab).
     * Schools can override this via configuration in a future iteration.
     *
     * <pre>
     * Monthly gross ≤ ₹7,500     → ₹0
     * ₹7,501 – ₹10,000           → ₹175
     * > ₹10,000 (non-February)   → ₹200
     * > ₹10,000 (February)       → ₹300
     * </pre>
     */
    private BigDecimal computeProfessionalTax(BigDecimal monthlyGross, int month) {
        int gross = monthlyGross.intValue();
        if (gross <= 7500) return BigDecimal.ZERO;
        if (gross <= 10000) return new BigDecimal("175.00");
        // February gets an extra ₹100 (annual balance collection)
        return month == Month.FEBRUARY.getValue()
            ? new BigDecimal("300.00")
            : new BigDecimal("200.00");
    }

    // ── TDS ────────────────────────────────────────────────────────────────────

    /**
     * Monthly TDS = (annual tax liability) / 12.
     *
     * <p><b>Old regime:</b>
     * Annual taxable = annualGross − standardDeduction(₹50,000) − 80C(max₹1.5L) − HRA exemption − other
     * Slabs: 0→2.5L: 0%, 2.5→5L: 5%, 5→10L: 20%, 10L+: 30%
     * Rebate u/s 87A: if taxable ≤ ₹5L → tax = 0
     *
     * <p><b>New regime (FY 2024-25):</b>
     * Annual taxable = annualGross − standardDeduction(₹75,000)
     * Slabs: 0→3L: 0%, 3→7L: 5%, 7→10L: 10%, 10→12L: 15%, 12→15L: 20%, 15L+: 30%
     * Rebate u/s 87A: if taxable ≤ ₹7L → tax = 0
     */
    private BigDecimal computeTds(SalaryStructure s, BigDecimal effectiveMonthlyGross) {
        // Annualise using current month effective gross as projection
        BigDecimal annualGross = effectiveMonthlyGross.multiply(BigDecimal.valueOf(12));

        BigDecimal annualTaxable;
        double annualTax;

        if (s.getTaxRegime() == TaxRegime.OLD) {
            // Standard deduction
            BigDecimal deductions = OLD_STANDARD_DEDUCTION;
            // 80C (max ₹1,50,000)
            BigDecimal d80c = s.getDeclared80c() != null ? s.getDeclared80c() : BigDecimal.ZERO;
            deductions = deductions.add(d80c.min(SECTION_80C_LIMIT));
            // HRA exemption declared
            BigDecimal hra = s.getDeclaredHraExemption() != null ? s.getDeclaredHraExemption() : BigDecimal.ZERO;
            deductions = deductions.add(hra);
            // Other exemptions
            BigDecimal other = s.getDeclaredOtherExemptions() != null ? s.getDeclaredOtherExemptions() : BigDecimal.ZERO;
            deductions = deductions.add(other);

            annualTaxable = annualGross.subtract(deductions).max(BigDecimal.ZERO);

            // Rebate 87A — old regime taxable ≤ ₹5L
            if (annualTaxable.compareTo(new BigDecimal("500000")) <= 0) return BigDecimal.ZERO;

            annualTax = computeOldRegimeTax(annualTaxable.doubleValue());

        } else {
            // New regime
            annualTaxable = annualGross.subtract(NEW_STANDARD_DEDUCTION).max(BigDecimal.ZERO);

            // Rebate 87A — new regime taxable ≤ ₹7L
            if (annualTaxable.compareTo(new BigDecimal("700000")) <= 0) return BigDecimal.ZERO;

            annualTax = computeNewRegimeTax(annualTaxable.doubleValue());
        }

        // Add 4% health & education cess
        annualTax = annualTax * 1.04;

        // Monthly TDS
        return BigDecimal.valueOf(annualTax / 12).setScale(2, RoundingMode.HALF_UP);
    }

    private double computeOldRegimeTax(double taxable) {
        double tax = 0;
        if (taxable > 1_000_000) {
            tax += (taxable - 1_000_000) * 0.30;
            taxable = 1_000_000;
        }
        if (taxable > 500_000) {
            tax += (taxable - 500_000) * 0.20;
            taxable = 500_000;
        }
        if (taxable > 250_000) {
            tax += (taxable - 250_000) * 0.05;
        }
        return tax;
    }

    private double computeNewRegimeTax(double taxable) {
        double tax = 0;
        // 15L+
        if (taxable > 1_500_000) {
            tax += (taxable - 1_500_000) * 0.30;
            taxable = 1_500_000;
        }
        // 12-15L
        if (taxable > 1_200_000) {
            tax += (taxable - 1_200_000) * 0.20;
            taxable = 1_200_000;
        }
        // 10-12L
        if (taxable > 1_000_000) {
            tax += (taxable - 1_000_000) * 0.15;
            taxable = 1_000_000;
        }
        // 7-10L
        if (taxable > 700_000) {
            tax += (taxable - 700_000) * 0.10;
            taxable = 700_000;
        }
        // 3-7L
        if (taxable > 300_000) {
            tax += (taxable - 300_000) * 0.05;
        }
        return tax;
    }
}
