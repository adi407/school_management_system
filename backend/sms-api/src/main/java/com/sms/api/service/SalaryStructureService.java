package com.sms.api.service;

import com.sms.api.dto.payroll.CreateSalaryStructureRequest;
import com.sms.api.dto.payroll.SalaryStructureDto;
import com.sms.api.entity.SalaryStructure;
import com.sms.api.entity.User;
import com.sms.api.repository.SalaryStructureRepository;
import com.sms.api.repository.UserRepository;
import com.sms.core.enums.TaxRegime;
import com.sms.core.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SalaryStructureService {

    private final SalaryStructureRepository salaryRepo;
    private final UserRepository            userRepository;

    public SalaryStructureService(SalaryStructureRepository salaryRepo,
                                   UserRepository userRepository) {
        this.salaryRepo    = salaryRepo;
        this.userRepository = userRepository;
    }

    // ── Create ────────────────────────────────────────────────────────────────

    public SalaryStructureDto create(UUID schoolId, CreateSalaryStructureRequest req) {
        User staff = userRepository.findById(req.staffId())
            .orElseThrow(() -> new ResourceNotFoundException("Staff User", req.staffId()));

        // Deactivate any currently active structure for this staff that overlaps
        deactivateExisting(req.staffId(), schoolId, req.effectiveFrom());

        SalaryStructure s = new SalaryStructure();
        s.setSchoolId(schoolId);
        s.setStaff(staff);
        applyRequest(s, req);
        return toDto(salaryRepo.save(s));
    }

    // ── Update (create a new revision; old one is closed off) ────────────────

    public SalaryStructureDto update(UUID schoolId, UUID structureId, CreateSalaryStructureRequest req) {
        SalaryStructure existing = salaryRepo.findByIdAndSchoolId(structureId, schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("SalaryStructure", structureId));

        // Close the old structure as of effectiveFrom - 1 day
        existing.setEffectiveTo(req.effectiveFrom().minusDays(1));
        existing.setActive(false);
        salaryRepo.save(existing);

        // Create new revision
        User staff = existing.getStaff();
        SalaryStructure newRev = new SalaryStructure();
        newRev.setSchoolId(schoolId);
        newRev.setStaff(staff);
        applyRequest(newRev, req);
        return toDto(salaryRepo.save(newRev));
    }

    // ── List all structures for school ────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<SalaryStructureDto> listBySchool(UUID schoolId) {
        return salaryRepo.findBySchoolIdOrderByCreatedAtDesc(schoolId)
            .stream().map(this::toDto).toList();
    }

    // ── List for a specific staff member ──────────────────────────────────────

    @Transactional(readOnly = true)
    public List<SalaryStructureDto> listByStaff(UUID schoolId, UUID staffId) {
        return salaryRepo.findByStaffIdAndSchoolId(staffId, schoolId)
            .stream().map(this::toDto).toList();
    }

    // ── Update declarations only (staff submits own declarations) ────────────

    public SalaryStructureDto updateDeclarations(UUID schoolId, UUID structureId,
                                                   BigDecimal declared80c,
                                                   BigDecimal declaredHra,
                                                   BigDecimal declaredOther) {
        SalaryStructure s = salaryRepo.findByIdAndSchoolId(structureId, schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("SalaryStructure", structureId));

        if (declared80c  != null) s.setDeclared80c(declared80c);
        if (declaredHra  != null) s.setDeclaredHraExemption(declaredHra);
        if (declaredOther != null) s.setDeclaredOtherExemptions(declaredOther);

        return toDto(salaryRepo.save(s));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void deactivateExisting(UUID staffId, UUID schoolId, LocalDate newEffectiveFrom) {
        List<SalaryStructure> active = salaryRepo.findActiveForStaffOnDate(staffId, schoolId, newEffectiveFrom);
        active.forEach(s -> {
            s.setEffectiveTo(newEffectiveFrom.minusDays(1));
            s.setActive(false);
        });
        if (!active.isEmpty()) salaryRepo.saveAll(active);
    }

    private void applyRequest(SalaryStructure s, CreateSalaryStructureRequest req) {
        s.setBasicSalary(req.basicSalary());
        s.setHraAmount(orZero(req.hraAmount()));
        s.setDaAmount(orZero(req.daAmount()));
        s.setTaAmount(orZero(req.taAmount()));
        s.setMedicalAllowance(orZero(req.medicalAllowance()));
        s.setOtherAllowances(orZero(req.otherAllowances()));
        s.setPfEnrolled(req.pfEnrolled());
        s.setPfWageCeiling(req.pfWageCeiling() != null ? req.pfWageCeiling() : new BigDecimal("15000"));
        s.setTaxRegime(req.taxRegime() != null ? req.taxRegime() : TaxRegime.NEW);
        s.setDeclared80c(orZero(req.declared80c()));
        s.setDeclaredHraExemption(orZero(req.declaredHraExemption()));
        s.setDeclaredOtherExemptions(orZero(req.declaredOtherExemptions()));
        s.setEffectiveFrom(req.effectiveFrom());
        s.setEffectiveTo(req.effectiveTo());
        s.setActive(true);
    }

    private BigDecimal orZero(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private SalaryStructureDto toDto(SalaryStructure s) {
        User staff = s.getStaff();
        return new SalaryStructureDto(
            s.getId(),
            staff != null ? staff.getId()       : null,
            staff != null ? staff.getFullName() : null,
            staff != null ? staff.getEmail()    : null,
            staff != null ? staff.getRole().name() : null,
            s.getBasicSalary(),
            s.getHraAmount(),
            s.getDaAmount(),
            s.getTaAmount(),
            s.getMedicalAllowance(),
            s.getOtherAllowances(),
            s.computeGross(),
            s.isPfEnrolled(),
            s.getPfWageCeiling(),
            s.getTaxRegime(),
            s.getDeclared80c(),
            s.getDeclaredHraExemption(),
            s.getDeclaredOtherExemptions(),
            s.getEffectiveFrom(),
            s.getEffectiveTo(),
            s.isActive()
        );
    }
}
