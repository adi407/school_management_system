package com.sms.api.repository;

import com.sms.api.entity.BookIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookIssueRepository extends JpaRepository<BookIssue, UUID> {
    List<BookIssue> findBySchoolIdOrderByIssueDateDesc(UUID schoolId);
    List<BookIssue> findBySchoolIdAndIsReturnedFalseOrderByDueDateAsc(UUID schoolId);
    Optional<BookIssue> findByIdAndSchoolId(UUID id, UUID schoolId);
    long countBySchoolIdAndIsReturnedFalse(UUID schoolId);
    long countBySchoolIdAndIsReturnedFalseAndDueDateBefore(UUID schoolId, LocalDate date);
}
