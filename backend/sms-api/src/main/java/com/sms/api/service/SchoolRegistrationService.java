package com.sms.api.service;

import com.sms.api.dto.registration.*;
import com.sms.api.dto.school.CreateSchoolRequest;
import com.sms.api.dto.school.SchoolDto;
import com.sms.api.entity.SchoolRegistration;
import com.sms.api.entity.SchoolRegistration.RegistrationStatus;
import com.sms.api.repository.SchoolRegistrationRepository;
import com.sms.api.repository.SchoolRepository;
import com.sms.api.repository.UserRepository;
import com.sms.core.enums.BoardType;
import com.sms.core.enums.SubscriptionTier;
import com.sms.core.exception.DuplicateResourceException;
import com.sms.core.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Service
@Transactional
public class SchoolRegistrationService {

    private final SchoolRegistrationRepository registrationRepo;
    private final SchoolRepository schoolRepo;
    private final UserRepository userRepo;
    private final SchoolService schoolService;
    private final EmailService emailService;

    public SchoolRegistrationService(
        SchoolRegistrationRepository registrationRepo,
        SchoolRepository schoolRepo,
        UserRepository userRepo,
        SchoolService schoolService,
        EmailService emailService
    ) {
        this.registrationRepo = registrationRepo;
        this.schoolRepo = schoolRepo;
        this.userRepo = userRepo;
        this.schoolService = schoolService;
        this.emailService = emailService;
    }

    public SchoolRegistrationDto submitRegistration(SchoolRegistrationRequest req) {
        String code = req.schoolCode().toUpperCase();

        if (registrationRepo.existsBySchoolCode(code)) {
            throw new DuplicateResourceException("School code '" + code + "' already has a pending registration");
        }
        if (schoolRepo.existsByCode(code)) {
            throw new DuplicateResourceException("School code '" + code + "' already exists");
        }
        if (registrationRepo.existsByAdminEmail(req.adminEmail())) {
            throw new DuplicateResourceException("Email '" + req.adminEmail() + "' already has a pending registration");
        }
        if (userRepo.existsByEmail(req.adminEmail())) {
            throw new DuplicateResourceException("Email '" + req.adminEmail() + "' is already in use");
        }

        SchoolRegistration reg = new SchoolRegistration();
        reg.setSchoolName(req.schoolName());
        reg.setSchoolCode(code);
        reg.setBoard(parseBoard(req.board()));
        reg.setRequestedTier(parseTier(req.requestedTier()));
        reg.setAddress(req.address());
        reg.setCity(req.city());
        reg.setState(req.state());
        reg.setPhone(req.phone());
        reg.setSchoolEmail(req.schoolEmail());
        reg.setWebsite(req.website());
        reg.setStudentCount(req.studentCount());
        reg.setAdminName(req.adminName());
        reg.setAdminEmail(req.adminEmail());
        reg.setAdminPhone(req.adminPhone());
        reg.setAdminDesignation(req.adminDesignation());
        reg.setMessage(req.message());

        reg = registrationRepo.save(reg);

        emailService.sendRegistrationConfirmation(reg);
        emailService.sendRegistrationNotification(reg);

        return toDto(reg);
    }

    @Transactional(readOnly = true)
    public Page<SchoolRegistrationDto> listRegistrations(String status, Pageable pageable) {
        if (status != null && !status.isBlank()) {
            RegistrationStatus s = RegistrationStatus.valueOf(status.toUpperCase());
            return registrationRepo.findByStatusOrderByCreatedAtDesc(s, pageable).map(this::toDto);
        }
        return registrationRepo.findAllByOrderByCreatedAtDesc(pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public SchoolRegistrationDto getRegistration(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public long countPending() {
        return registrationRepo.countByStatus(RegistrationStatus.PENDING_APPROVAL);
    }

    public SchoolRegistrationDto approveRegistration(UUID id, ApproveRegistrationRequest req) {
        SchoolRegistration reg = findOrThrow(id);

        if (reg.getStatus() != RegistrationStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Registration is already " + reg.getStatus());
        }

        SubscriptionTier tier = req.subscriptionTier() != null
            ? parseTier(req.subscriptionTier())
            : reg.getRequestedTier();

        LocalDate expiry = tier == SubscriptionTier.FREE ? null : LocalDate.now().plusYears(1);

        CreateSchoolRequest schoolReq = new CreateSchoolRequest(
            reg.getSchoolName(),
            reg.getSchoolCode(),
            reg.getBoard(),
            tier,
            reg.getAddress(),
            reg.getPhone(),
            reg.getSchoolEmail(),
            null,
            null,
            expiry,
            reg.getAdminEmail(),
            req.adminPassword(),
            reg.getAdminName()
        );

        SchoolDto school = schoolService.createSchool(schoolReq);

        reg.setStatus(RegistrationStatus.APPROVED);
        reg.setApprovedSchoolId(school.id());
        reg.setReviewedAt(Instant.now());
        registrationRepo.save(reg);

        emailService.sendRegistrationApproved(reg, req.adminPassword());

        return toDto(reg);
    }

    public SchoolRegistrationDto rejectRegistration(UUID id, RejectRegistrationRequest req) {
        SchoolRegistration reg = findOrThrow(id);

        if (reg.getStatus() != RegistrationStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Registration is already " + reg.getStatus());
        }

        reg.setStatus(RegistrationStatus.REJECTED);
        reg.setRejectionReason(req.reason());
        reg.setReviewedAt(Instant.now());
        registrationRepo.save(reg);

        emailService.sendRegistrationRejected(reg);

        return toDto(reg);
    }

    private SchoolRegistration findOrThrow(UUID id) {
        return registrationRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("SchoolRegistration", id));
    }

    private BoardType parseBoard(String board) {
        if (board == null || board.isBlank()) return BoardType.CBSE;
        try { return BoardType.valueOf(board.toUpperCase()); }
        catch (IllegalArgumentException e) { return BoardType.CBSE; }
    }

    private SubscriptionTier parseTier(String tier) {
        if (tier == null || tier.isBlank()) return SubscriptionTier.FREE;
        try { return SubscriptionTier.valueOf(tier.toUpperCase()); }
        catch (IllegalArgumentException e) { return SubscriptionTier.FREE; }
    }

    private SchoolRegistrationDto toDto(SchoolRegistration r) {
        return new SchoolRegistrationDto(
            r.getId(),
            r.getSchoolName(),
            r.getSchoolCode(),
            r.getBoard().name(),
            r.getRequestedTier().name(),
            r.getAddress(),
            r.getCity(),
            r.getState(),
            r.getPhone(),
            r.getSchoolEmail(),
            r.getWebsite(),
            r.getStudentCount(),
            r.getAdminName(),
            r.getAdminEmail(),
            r.getAdminPhone(),
            r.getAdminDesignation(),
            r.getMessage(),
            r.getStatus().name(),
            r.getRejectionReason(),
            r.getApprovedSchoolId(),
            r.getCreatedAt(),
            r.getReviewedAt()
        );
    }
}
