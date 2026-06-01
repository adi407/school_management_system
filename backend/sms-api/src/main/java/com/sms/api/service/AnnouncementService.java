package com.sms.api.service;

import com.sms.api.dto.announcement.AnnouncementDto;
import com.sms.api.dto.announcement.CreateAnnouncementRequest;
import com.sms.api.entity.Announcement;
import com.sms.api.entity.User;
import com.sms.api.repository.AnnouncementRepository;
import com.sms.api.repository.UserRepository;
import com.sms.core.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final UserRepository         userRepository;

    public AnnouncementService(AnnouncementRepository announcementRepository,
                                UserRepository userRepository) {
        this.announcementRepository = announcementRepository;
        this.userRepository         = userRepository;
    }

    @Transactional(readOnly = true)
    public List<AnnouncementDto> list(UUID schoolId) {
        return announcementRepository
            .findBySchoolIdOrderByIsPinnedDescPublishedAtDesc(schoolId)
            .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<AnnouncementDto> listActive(UUID schoolId) {
        return announcementRepository
            .findBySchoolIdAndExpiresAtIsNullOrExpiresAtAfterOrderByIsPinnedDescPublishedAtDesc(
                schoolId, Instant.now())
            .stream().map(this::toDto).toList();
    }

    public AnnouncementDto create(UUID schoolId, UUID publishedByUserId,
                                   CreateAnnouncementRequest req) {
        User publisher = userRepository.findById(publishedByUserId)
            .orElseThrow(() -> new ResourceNotFoundException("User", publishedByUserId));

        Announcement a = new Announcement();
        a.setSchoolId(schoolId);
        a.setTitle(req.title());
        a.setBody(req.body());
        a.setTargetRoles(req.targetRoles() != null ? req.targetRoles() : List.of());
        a.setPublishedBy(publisher);
        a.setPublishedAt(Instant.now());
        a.setExpiresAt(req.expiresAt());
        a.setPinned(req.isPinned());

        return toDto(announcementRepository.save(a));
    }

    public AnnouncementDto update(UUID schoolId, UUID announcementId,
                                   CreateAnnouncementRequest req) {
        Announcement a = announcementRepository.findById(announcementId)
            .filter(x -> schoolId.equals(x.getSchoolId()))
            .orElseThrow(() -> new ResourceNotFoundException("Announcement", announcementId));

        a.setTitle(req.title());
        a.setBody(req.body());
        if (req.targetRoles() != null) a.setTargetRoles(req.targetRoles());
        a.setExpiresAt(req.expiresAt());
        a.setPinned(req.isPinned());

        return toDto(announcementRepository.save(a));
    }

    public void delete(UUID schoolId, UUID announcementId) {
        Announcement a = announcementRepository.findById(announcementId)
            .filter(x -> schoolId.equals(x.getSchoolId()))
            .orElseThrow(() -> new ResourceNotFoundException("Announcement", announcementId));
        announcementRepository.delete(a);
    }

    private AnnouncementDto toDto(Announcement a) {
        User pb = a.getPublishedBy();
        return new AnnouncementDto(
            a.getId(),
            a.getTitle(),
            a.getBody(),
            a.getTargetRoles(),
            pb != null ? pb.getId()       : null,
            pb != null ? pb.getFullName() : null,
            a.getPublishedAt(),
            a.getExpiresAt(),
            a.isPinned()
        );
    }
}
