package com.sms.api.service;

import com.sms.api.dto.staff.CreateStaffRequest;
import com.sms.api.dto.staff.StaffDto;
import com.sms.api.dto.staff.UpdateStaffRequest;
import com.sms.api.entity.School;
import com.sms.api.entity.User;
import com.sms.api.repository.SchoolRepository;
import com.sms.api.repository.UserRepository;
import com.sms.core.enums.Role;
import com.sms.core.exception.ResourceNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class StaffService {

    private static final Set<Role> STAFF_ROLES = Set.of(
        Role.TEACHER, Role.ACCOUNTANT, Role.LIBRARIAN,
        Role.TRANSPORT_MANAGER, Role.HOSTEL_WARDEN
    );

    private final UserRepository   userRepository;
    private final SchoolRepository schoolRepository;
    private final PasswordEncoder  passwordEncoder;
    private final EmailService     emailService;

    public StaffService(UserRepository userRepository,
                        SchoolRepository schoolRepository,
                        PasswordEncoder passwordEncoder,
                        EmailService emailService) {
        this.userRepository   = userRepository;
        this.schoolRepository = schoolRepository;
        this.passwordEncoder  = passwordEncoder;
        this.emailService     = emailService;
    }

    @Transactional(readOnly = true)
    public List<StaffDto> listStaff(UUID schoolId) {
        return userRepository.findAllBySchoolId(schoolId).stream()
            .filter(u -> STAFF_ROLES.contains(u.getRole()))
            .map(this::toDto)
            .toList();
    }

    public StaffDto createStaff(UUID schoolId, CreateStaffRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new IllegalArgumentException("Email already in use: " + req.email());
        }

        Role role = Role.valueOf(req.role());
        if (!STAFF_ROLES.contains(role)) {
            throw new IllegalArgumentException("Invalid staff role: " + req.role());
        }

        School school = schoolRepository.findById(schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("School", schoolId));

        User user = new User();
        user.setSchool(school);
        user.setEmail(req.email());
        user.setFirstName(req.firstName());
        user.setLastName(req.lastName());
        user.setPhone(req.phone());
        user.setDepartment(req.department());
        user.setRole(role);
        user.setActive(true);

        String rawPassword = (req.password() != null && !req.password().isBlank())
            ? req.password()
            : "Welcome@1234";
        user.setPasswordHash(passwordEncoder.encode(rawPassword));

        User saved = userRepository.save(user);
        emailService.sendWelcome(saved, school.getName());
        return toDto(saved);
    }

    public StaffDto updateStaff(UUID schoolId, UUID staffId, UpdateStaffRequest req) {
        User user = userRepository.findById(staffId)
            .filter(u -> u.getSchool() != null && schoolId.equals(u.getSchool().getId()))
            .orElseThrow(() -> new ResourceNotFoundException("Staff", staffId));

        if (req.firstName()  != null) user.setFirstName(req.firstName());
        if (req.lastName()   != null) user.setLastName(req.lastName());
        if (req.phone()      != null) user.setPhone(req.phone());
        if (req.department() != null) user.setDepartment(req.department());
        if (req.isActive()   != null) user.setActive(req.isActive());

        if (req.role() != null) {
            Role role = Role.valueOf(req.role());
            if (!STAFF_ROLES.contains(role)) {
                throw new IllegalArgumentException("Invalid staff role: " + req.role());
            }
            user.setRole(role);
        }

        return toDto(userRepository.save(user));
    }

    /**
     * Resets the password for any user belonging to the given school.
     * Intended for super-admin use only — the controller enforces that role constraint.
     * Uses tenant-safe lookup (schoolId + userId must match) so a super-admin
     * cannot accidentally reset a user from a different school.
     */
    public void resetUserPassword(UUID schoolId, UUID userId, String newPassword) {
        User user = userRepository.findByIdAndSchoolId(userId, schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void deleteStaff(UUID schoolId, UUID staffId) {
        User user = userRepository.findById(staffId)
            .filter(u -> u.getSchool() != null && schoolId.equals(u.getSchool().getId()))
            .filter(u -> STAFF_ROLES.contains(u.getRole()))
            .orElseThrow(() -> new ResourceNotFoundException("Staff", staffId));
        user.setActive(false);
        userRepository.save(user);
    }

    private StaffDto toDto(User u) {
        return new StaffDto(
            u.getId(),
            u.getEmail(),
            u.getFirstName(),
            u.getLastName(),
            u.getFullName(),
            u.getPhone(),
            u.getDepartment(),
            u.getRole().name(),
            u.isActive(),
            u.getCreatedAt()
        );
    }
}
