package com.sms.api.service;

import com.sms.api.dto.payroll.MarkStaffAttendanceRequest;
import com.sms.api.dto.payroll.StaffAttendanceDto;
import com.sms.api.entity.StaffAttendance;
import com.sms.api.entity.User;
import com.sms.api.repository.StaffAttendanceRepository;
import com.sms.api.repository.UserRepository;
import com.sms.core.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class StaffAttendanceService {

    private final StaffAttendanceRepository staffAttRepo;
    private final UserRepository            userRepository;

    public StaffAttendanceService(StaffAttendanceRepository staffAttRepo,
                                  UserRepository userRepository) {
        this.staffAttRepo  = staffAttRepo;
        this.userRepository = userRepository;
    }

    // ── Mark bulk attendance for multiple staff on a date ─────────────────────

    public List<StaffAttendanceDto> markAttendance(UUID schoolId,
                                                    UUID markedByUserId,
                                                    MarkStaffAttendanceRequest req) {
        User marker = userRepository.findById(markedByUserId)
            .orElseThrow(() -> new ResourceNotFoundException("User", markedByUserId));

        // Load existing records for this date to allow upsert behaviour
        List<UUID> staffIds = req.entries().stream()
            .map(MarkStaffAttendanceRequest.StaffAttendanceEntry::staffId)
            .toList();

        Map<UUID, StaffAttendance> existing = staffAttRepo
            .findBySchoolIdAndAttendanceDateOrderByStaffId(schoolId, req.date())
            .stream()
            .filter(sa -> staffIds.contains(sa.getStaff().getId()))
            .collect(Collectors.toMap(sa -> sa.getStaff().getId(), Function.identity()));

        List<StaffAttendance> toSave = req.entries().stream().map(entry -> {
            User staff = userRepository.findById(entry.staffId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff User", entry.staffId()));

            StaffAttendance sa = existing.getOrDefault(entry.staffId(), new StaffAttendance());
            sa.setSchoolId(schoolId);
            sa.setStaff(staff);
            sa.setAttendanceDate(req.date());
            sa.setStatus(entry.status());
            sa.setRemarks(entry.remarks());
            sa.setMarkedBy(marker);
            return sa;
        }).collect(Collectors.toList());

        return staffAttRepo.saveAll(toSave).stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    // ── Get school-wide roll for a date ───────────────────────────────────────

    @Transactional(readOnly = true)
    public List<StaffAttendanceDto> getDayRoll(UUID schoolId, LocalDate date) {
        return staffAttRepo.findBySchoolIdAndAttendanceDateOrderByStaffId(schoolId, date)
            .stream().map(this::toDto).toList();
    }

    // ── History for a single staff member ─────────────────────────────────────

    @Transactional(readOnly = true)
    public List<StaffAttendanceDto> getStaffHistory(UUID schoolId, UUID staffId,
                                                     LocalDate from, LocalDate to) {
        return staffAttRepo
            .findByStaffIdAndAttendanceDateBetweenOrderByAttendanceDateAsc(staffId, from, to)
            .stream().map(this::toDto).toList();
    }

    // ── Compute effective present days for payroll (HALF_DAY = 0.5) ──────────

    /**
     * Returns effective present days for a staff member in a date range.
     * PRESENT   = 1.0 day
     * HALF_DAY  = 0.5 day
     * Everything else = 0
     */
    public java.math.BigDecimal computeEffectivePresentDays(UUID staffId, LocalDate from, LocalDate to) {
        long fullDays = staffAttRepo.countPresentDays(staffId, from, to);
        long halfDays = staffAttRepo.countHalfDays(staffId, from, to);
        // fullDays already includes HALF_DAY count (due to query), subtract half-day count then add 0.5 each
        long fullOnlyDays = fullDays - halfDays;
        double effective = fullOnlyDays + (halfDays * 0.5);
        return java.math.BigDecimal.valueOf(effective);
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private StaffAttendanceDto toDto(StaffAttendance sa) {
        User staff = sa.getStaff();
        return new StaffAttendanceDto(
            sa.getId(),
            staff != null ? staff.getId()       : null,
            staff != null ? staff.getFullName() : null,
            staff != null ? staff.getEmail()    : null,
            sa.getAttendanceDate(),
            sa.getStatus(),
            sa.getRemarks()
        );
    }
}
