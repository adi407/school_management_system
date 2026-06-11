package com.sms.api.repository;

import com.sms.api.entity.PtmBriefing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PtmBriefingRepository extends JpaRepository<PtmBriefing, UUID> {

    List<PtmBriefing> findByPtmMeetingIdOrderByStudentId(UUID ptmMeetingId);

    Optional<PtmBriefing> findByPtmMeetingIdAndStudentId(UUID ptmMeetingId, UUID studentId);

    long countByPtmMeetingId(UUID ptmMeetingId);
}
