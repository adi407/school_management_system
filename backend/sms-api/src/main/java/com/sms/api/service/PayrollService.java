package com.sms.api.service;

import com.sms.api.dto.payroll.PayrollRunDto;
import com.sms.api.dto.payroll.PayslipDto;
import com.sms.api.dto.payroll.TriggerPayrollRequest;
import com.sms.api.entity.Payslip;
import com.sms.api.entity.PayrollRun;
import com.sms.api.entity.SalaryStructure;
import com.sms.api.entity.User;
import com.sms.api.repository.PayrollRunRepository;
import com.sms.api.repository.PayslipRepository;
import com.sms.api.repository.SalaryStructureRepository;
import com.sms.api.repository.UserRepository;
import com.sms.core.enums.PayrollStatus;
import com.sms.core.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PayrollService {

    private final PayrollRunRepository        runRepo;
    private final PayslipRepository           payslipRepo;
    private final SalaryStructureRepository   salaryRepo;
    private final UserRepository              userRepo;
    private final StaffAttendanceService      attendanceService;
    private final PayrollCalculationEngine    engine;

    public PayrollService(PayrollRunRepository runRepo,
                          PayslipRepository payslipRepo,
                          SalaryStructureRepository salaryRepo,
                          UserRepository userRepo,
                          StaffAttendanceService attendanceService,
                          PayrollCalculationEngine engine) {
        this.runRepo           = runRepo;
        this.payslipRepo       = payslipRepo;
        this.salaryRepo        = salaryRepo;
        this.userRepo          = userRepo;
        this.attendanceService = attendanceService;
        this.engine            = engine;
    }

    // ── Trigger a new payroll run ─────────────────────────────────────────────

    /**
     * Computes payslips for every staff member with an active SalaryStructure
     * for the requested month/year. Pulls attendance data automatically to
     * calculate LOP deductions.
     */
    public PayrollRunDto triggerRun(UUID schoolId, UUID triggeredByUserId, TriggerPayrollRequest req) {
        // Prevent duplicate runs
        runRepo.findBySchoolIdAndRunMonthAndRunYear(schoolId, req.month(), req.year())
            .ifPresent(existing -> {
                throw new IllegalStateException(
                    "Payroll run for " + req.month() + "/" + req.year() + " already exists (status: " + existing.getStatus() + ")");
            });

        User triggeredBy = userRepo.findById(triggeredByUserId)
            .orElseThrow(() -> new ResourceNotFoundException("User", triggeredByUserId));

        int workingDays = req.totalWorkingDays() != null ? req.totalWorkingDays() : 26;

        // Create the run header
        PayrollRun run = new PayrollRun();
        run.setSchoolId(schoolId);
        run.setRunMonth(req.month());
        run.setRunYear(req.year());
        run.setTotalWorkingDays(workingDays);
        run.setStatus(PayrollStatus.DRAFT);
        run.setTriggeredBy(triggeredBy);
        run.setNotes(req.notes());
        run = runRepo.save(run);

        // Attendance range: first → last day of the pay month
        YearMonth ym   = YearMonth.of(req.year(), req.month());
        LocalDate from = ym.atDay(1);
        LocalDate to   = ym.atEndOfMonth();

        // Reference date for picking salary structure
        LocalDate structureRefDate = to;

        // Load all active salary structures for this school effective in the pay month
        List<SalaryStructure> structures = salaryRepo.findAllActiveForSchoolOnDate(schoolId, structureRefDate);

        List<Payslip> payslips = new ArrayList<>();

        BigDecimal totalGross    = BigDecimal.ZERO;
        BigDecimal totalPfEe     = BigDecimal.ZERO;
        BigDecimal totalPfEr     = BigDecimal.ZERO;
        BigDecimal totalEsiEe    = BigDecimal.ZERO;
        BigDecimal totalEsiEr    = BigDecimal.ZERO;
        BigDecimal totalPt       = BigDecimal.ZERO;
        BigDecimal totalTds      = BigDecimal.ZERO;
        BigDecimal totalLop      = BigDecimal.ZERO;
        BigDecimal totalNet      = BigDecimal.ZERO;

        for (SalaryStructure structure : structures) {
            User staff = structure.getStaff();

            // ── Attendance & LOP ──────────────────────────────────────────────
            BigDecimal effectivePresentDays = attendanceService.computeEffectivePresentDays(
                staff.getId(), from, to);

            BigDecimal grossSalary  = structure.computeGross();
            BigDecimal lopDeduction = engine.computeLopDeduction(grossSalary, workingDays, effectivePresentDays);
            BigDecimal effectiveGross = grossSalary.subtract(lopDeduction).max(BigDecimal.ZERO);

            int lopDays = BigDecimal.valueOf(workingDays).subtract(effectivePresentDays)
                .max(BigDecimal.ZERO).setScale(0, RoundingMode.HALF_UP).intValue();

            // ── Statutory deductions ──────────────────────────────────────────
            PayrollCalculationEngine.DeductionResult deductions =
                engine.calculate(structure, effectiveGross, req.month(), req.year());

            // ── Build payslip ─────────────────────────────────────────────────
            Payslip slip = new Payslip();
            slip.setSchoolId(schoolId);
            slip.setPayrollRun(run);
            slip.setStaff(staff);
            slip.setSalaryStructure(structure);

            slip.setBasicSalary(structure.getBasicSalary());
            slip.setHraAmount(structure.getHraAmount());
            slip.setDaAmount(structure.getDaAmount());
            slip.setTaAmount(structure.getTaAmount());
            slip.setMedicalAllowance(structure.getMedicalAllowance());
            slip.setOtherAllowances(structure.getOtherAllowances());
            slip.setGrossSalary(grossSalary);

            slip.setTotalWorkingDays(workingDays);
            slip.setPresentDays(effectivePresentDays.setScale(0, RoundingMode.HALF_UP).intValue());
            slip.setLopDays(lopDays);
            slip.setLopDeduction(lopDeduction);
            slip.setEffectiveGross(effectiveGross);

            slip.setPfEmployee(deductions.pfEmployee());
            slip.setPfEmployer(deductions.pfEmployer());
            slip.setEsiEmployee(deductions.esiEmployee());
            slip.setEsiEmployer(deductions.esiEmployer());
            slip.setProfessionalTax(deductions.professionalTax());
            slip.setTds(deductions.tds());
            slip.setTotalDeductions(deductions.totalEmployeeDeductions());

            BigDecimal netSalary = effectiveGross.subtract(deductions.totalEmployeeDeductions())
                .max(BigDecimal.ZERO);
            slip.setNetSalary(netSalary);

            payslips.add(slip);

            // Accumulate run totals
            totalGross = totalGross.add(grossSalary);
            totalPfEe  = totalPfEe.add(deductions.pfEmployee());
            totalPfEr  = totalPfEr.add(deductions.pfEmployer());
            totalEsiEe = totalEsiEe.add(deductions.esiEmployee());
            totalEsiEr = totalEsiEr.add(deductions.esiEmployer());
            totalPt    = totalPt.add(deductions.professionalTax());
            totalTds   = totalTds.add(deductions.tds());
            totalLop   = totalLop.add(lopDeduction);
            totalNet   = totalNet.add(netSalary);
        }

        payslipRepo.saveAll(payslips);

        // Update run aggregates
        run.setTotalGross(totalGross);
        run.setTotalPfEmployee(totalPfEe);
        run.setTotalPfEmployer(totalPfEr);
        run.setTotalEsiEmployee(totalEsiEe);
        run.setTotalEsiEmployer(totalEsiEr);
        run.setTotalProfessionalTax(totalPt);
        run.setTotalTds(totalTds);
        run.setTotalLopDeduction(totalLop);
        run.setTotalNetPayout(totalNet);
        run = runRepo.save(run);

        return toRunDto(run, payslips.size());
    }

    // ── Approve run ───────────────────────────────────────────────────────────

    public PayrollRunDto approveRun(UUID schoolId, UUID runId, UUID approvedByUserId) {
        PayrollRun run = getRunOrThrow(schoolId, runId);
        if (run.getStatus() != PayrollStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT runs can be approved");
        }
        User approver = userRepo.findById(approvedByUserId)
            .orElseThrow(() -> new ResourceNotFoundException("User", approvedByUserId));

        run.setStatus(PayrollStatus.APPROVED);
        run.setApprovedBy(approver);
        run.setApprovedAt(Instant.now());
        run = runRepo.save(run);
        int count = payslipRepo.findByPayrollRunId(runId).size();
        return toRunDto(run, count);
    }

    // ── Mark as paid ──────────────────────────────────────────────────────────

    public PayrollRunDto markPaid(UUID schoolId, UUID runId) {
        PayrollRun run = getRunOrThrow(schoolId, runId);
        if (run.getStatus() != PayrollStatus.APPROVED) {
            throw new IllegalStateException("Only APPROVED runs can be marked as PAID");
        }
        run.setStatus(PayrollStatus.PAID);
        run.setPaidAt(Instant.now());
        run = runRepo.save(run);
        int count = payslipRepo.findByPayrollRunId(runId).size();
        return toRunDto(run, count);
    }

    // ── List runs ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<PayrollRunDto> listRuns(UUID schoolId) {
        return runRepo.findBySchoolIdOrderByRunYearDescRunMonthDesc(schoolId).stream()
            .map(r -> toRunDto(r, payslipRepo.findByPayrollRunId(r.getId()).size()))
            .toList();
    }

    // ── Get payslips for a run ────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<PayslipDto> getPayslipsForRun(UUID schoolId, UUID runId) {
        getRunOrThrow(schoolId, runId);
        return payslipRepo.findByPayrollRunId(runId).stream().map(this::toSlipDto).toList();
    }

    // ── Get single payslip ────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PayslipDto getPayslip(UUID schoolId, UUID payslipId) {
        Payslip slip = payslipRepo.findByIdAndSchoolId(payslipId, schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("Payslip", payslipId));
        return toSlipDto(slip);
    }

    // ── Staff's own payslip history ───────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<PayslipDto> getMyPayslips(UUID schoolId, UUID staffId) {
        return payslipRepo.findByStaffIdOrderByDateDesc(staffId, schoolId).stream()
            .map(this::toSlipDto).toList();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private PayrollRun getRunOrThrow(UUID schoolId, UUID runId) {
        return runRepo.findByIdAndSchoolId(runId, schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("PayrollRun", runId));
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private PayrollRunDto toRunDto(PayrollRun r, int count) {
        return new PayrollRunDto(
            r.getId(), r.getRunMonth(), r.getRunYear(), r.getTotalWorkingDays(), r.getStatus(),
            r.getTotalGross(), r.getTotalPfEmployee(), r.getTotalPfEmployer(),
            r.getTotalEsiEmployee(), r.getTotalEsiEmployer(),
            r.getTotalProfessionalTax(), r.getTotalTds(),
            r.getTotalLopDeduction(), r.getTotalNetPayout(),
            r.getTriggeredBy() != null ? r.getTriggeredBy().getFullName() : null,
            r.getApprovedBy()  != null ? r.getApprovedBy().getFullName()  : null,
            r.getApprovedAt(), r.getPaidAt(), r.getNotes(), count
        );
    }

    private PayslipDto toSlipDto(Payslip p) {
        User staff = p.getStaff();
        PayrollRun run = p.getPayrollRun();
        return new PayslipDto(
            p.getId(),
            run  != null ? run.getId()       : null,
            run  != null ? run.getRunMonth() : 0,
            run  != null ? run.getRunYear()  : 0,
            staff != null ? staff.getId()          : null,
            staff != null ? staff.getFullName()    : null,
            staff != null ? staff.getEmail()       : null,
            staff != null ? staff.getRole().name() : null,
            staff != null ? staff.getDepartment()  : null,
            p.getBasicSalary(),     p.getHraAmount(),
            p.getDaAmount(),        p.getTaAmount(),
            p.getMedicalAllowance(),p.getOtherAllowances(),
            p.getGrossSalary(),
            p.getTotalWorkingDays(),p.getPresentDays(),
            p.getLopDays(),         p.getLopDeduction(),
            p.getEffectiveGross(),
            p.getPfEmployee(),      p.getPfEmployer(),
            p.getEsiEmployee(),     p.getEsiEmployer(),
            p.getProfessionalTax(), p.getTds(),
            p.getTotalDeductions(), p.getNetSalary()
        );
    }
}
