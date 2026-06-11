package com.sms.api.repository;

import com.sms.api.entity.PtmMeeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PtmMeetingRepository extends JpaRepository<PtmMeeting, UUID> {

    List<PtmMeeting> findBySchoolIdOrderByMeetingDateDesc(UUID schoolId);

    List<PtmMeeting> findBySchoolIdAndMeetingDateBetweenOrderByMeetingDateAsc(
        UUID schoolId, LocalDate from, LocalDate to);

    Optional<PtmMeeting> findByIdAndSchoolId(UUID id, UUID schoolId);

    List<PtmMeeting> findBySchoolClassIdAndSchoolIdOrderByMeetingDateDesc(UUID classId, UUID schoolId);
}
