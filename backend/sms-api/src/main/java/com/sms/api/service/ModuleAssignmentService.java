package com.sms.api.service;

import com.sms.api.dto.module.AssignModuleRequest;
import com.sms.api.dto.module.MyModuleDto;
import com.sms.api.dto.module.StaffModuleAssignmentDto;
import com.sms.api.entity.StaffModuleAssignment;
import com.sms.api.entity.User;
import com.sms.api.repository.StaffModuleAssignmentRepository;
import com.sms.api.repository.UserRepository;
import com.sms.core.enums.ModulePermission;
import com.sms.core.enums.StaffModule;
import com.sms.core.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Manages which functional modules a staff member can access within a school.
 *
 * Key rules:
 *  - SUPER_ADMIN bypasses all module checks (enforced at the AOP layer).
 *  - SCHOOL_ADMIN founding user is auto-assigned every module on school creation.
 *  - One row per (school, user, module). Revoking sets isActive = false.
 *  - Re-assigning a previously revoked module re-activates the row.
 */
@Service
@Transactional
public class ModuleAssignmentService {

    private final StaffModuleAssignmentRepository assignmentRepo;
    private final UserRepository userRepository;

    public ModuleAssignmentService(StaffModuleAssignmentRepository assignmentRepo,
                                   UserRepository userRepository) {
        this.assignmentRepo = assignmentRepo;
        this.userRepository  = userRepository;
    }

    // ── Public API ─────────────────────────────────────────────────────────────

    /**
     * Assign (or update) a module for a staff member.
     * If a previously-revoked assignment exists it is re-activated in place.
     * Sub-permissions null/empty = full module access.
     */
    public StaffModuleAssignmentDto assignModule(UUID schoolId,
                                                  UUID staffId,
                                                  UUID assignedById,
                                                  AssignModuleRequest req) {
        User staff      = findUser(staffId, schoolId);
        User assignedBy = findUser(assignedById, schoolId);

        // Look for any existing row (active or revoked)
        StaffModuleAssignment assignment = assignmentRepo
            .findByStaffIdAndSchoolIdAndModule(staffId, schoolId, req.module())
            .orElseGet(StaffModuleAssignment::new);

        assignment.setStaff(staff);
        assignment.setSchoolId(schoolId);
        assignment.setModule(req.module());
        assignment.setSubPermissions(normalizeSubPermissions(req.subPermissions()));
        assignment.setAssignedBy(assignedBy);
        assignment.setAssignedAt(Instant.now());
        assignment.setActive(true);

        return toDto(assignmentRepo.save(assignment));
    }

    /**
     * Revoke a module from a staff member (soft-delete → isActive = false).
     */
    public void revokeModule(UUID schoolId, UUID staffId, StaffModule module) {
        assignmentRepo.findByStaffIdAndSchoolIdAndModuleAndIsActiveTrue(staffId, schoolId, module)
            .ifPresent(a -> {
                a.setActive(false);
                assignmentRepo.save(a);
            });
    }

    /**
     * Returns the active modules for the calling user — used at login to build the sidebar.
     */
    @Transactional(readOnly = true)
    public List<MyModuleDto> getMyModules(UUID staffId, UUID schoolId) {
        return assignmentRepo
            .findByStaffIdAndSchoolIdAndIsActiveTrue(staffId, schoolId)
            .stream()
            .map(a -> new MyModuleDto(a.getModule(), a.getSubPermissions()))
            .toList();
    }

    /**
     * All active + inactive assignments for a staff member — for the admin assignment panel.
     */
    @Transactional(readOnly = true)
    public List<StaffModuleAssignmentDto> getStaffModules(UUID schoolId, UUID staffId) {
        return assignmentRepo
            .findByStaffIdAndSchoolId(staffId, schoolId)
            .stream()
            .map(this::toDto)
            .toList();
    }

    /**
     * Every active assignment in the school — for the admin overview table.
     */
    @Transactional(readOnly = true)
    public List<StaffModuleAssignmentDto> getAllSchoolAssignments(UUID schoolId) {
        return assignmentRepo.findAllActiveBySchool(schoolId)
            .stream()
            .map(this::toDto)
            .toList();
    }

    // ── Permission checks (called by ModuleAuthorizationAspect) ───────────────

    /**
     * Does the user hold an active assignment for this module?
     */
    @Transactional(readOnly = true)
    public boolean hasModule(UUID userId, UUID schoolId, StaffModule module) {
        return assignmentRepo
            .findByStaffIdAndSchoolIdAndModuleAndIsActiveTrue(userId, schoolId, module)
            .isPresent();
    }

    /**
     * Does the user hold this module AND either has full access or the specific sub-permission?
     */
    @Transactional(readOnly = true)
    public boolean hasPermission(UUID userId, UUID schoolId,
                                  StaffModule module, String subPermission) {
        return assignmentRepo
            .findByStaffIdAndSchoolIdAndModuleAndIsActiveTrue(userId, schoolId, module)
            .map(a -> a.grantsPermission(subPermission))
            .orElse(false);
    }

    // ── Internal helpers ───────────────────────────────────────────────────────

    /**
     * Auto-assign ALL modules to the founding SCHOOL_ADMIN during school creation.
     * Sub-permissions are null on all rows → full access.
     */
    public void assignAllModules(UUID schoolId, UUID adminId) {
        User admin = userRepository.findById(adminId)
            .orElseThrow(() -> new ResourceNotFoundException("User", adminId));

        for (StaffModule module : StaffModule.values()) {
            StaffModuleAssignment a = assignmentRepo
                .findByStaffIdAndSchoolIdAndModule(adminId, schoolId, module)
                .orElseGet(StaffModuleAssignment::new);

            a.setStaff(admin);
            a.setSchoolId(schoolId);
            a.setModule(module);
            a.setSubPermissions(null);   // full access
            a.setAssignedBy(admin);       // self-assigned at creation
            a.setAssignedAt(Instant.now());
            a.setActive(true);
            assignmentRepo.save(a);
        }
    }

    private User findUser(UUID userId, UUID schoolId) {
        return userRepository.findByIdAndSchoolId(userId, schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    /**
     * Empty list → treat as null (full access).
     */
    private List<String> normalizeSubPermissions(List<String> subs) {
        return (subs == null || subs.isEmpty()) ? null : subs;
    }

    private StaffModuleAssignmentDto toDto(StaffModuleAssignment a) {
        User staff = a.getStaff();
        String name = (staff.getFirstName() != null ? staff.getFirstName() : "")
            + (staff.getLastName() != null ? " " + staff.getLastName() : "");
        return new StaffModuleAssignmentDto(
            a.getId(),
            staff.getId(),
            name.trim(),
            staff.getEmail(),
            a.getModule(),
            a.getSubPermissions(),
            a.isActive(),
            a.getAssignedAt()
        );
    }
}
