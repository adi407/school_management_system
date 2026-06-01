package com.sms.api.repository;

import com.sms.api.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, UUID> {

    List<Announcement> findBySchoolIdOrderByIsPinnedDescPublishedAtDesc(UUID schoolId);

    List<Announcement> findBySchoolIdAndExpiresAtIsNullOrExpiresAtAfterOrderByIsPinnedDescPublishedAtDesc(
        UUID schoolId, Instant now);
}
