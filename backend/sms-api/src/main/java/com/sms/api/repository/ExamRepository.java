package com.sms.api.repository;

import com.sms.api.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExamRepository extends JpaRepository<Exam, UUID> {
    List<Exam> findBySchoolIdOrderByStartDateDesc(UUID schoolId);
    Optional<Exam> findByIdAndSchoolId(UUID id, UUID schoolId);
    long countBySchoolIdAndStatusAndStartDateAfter(UUID schoolId, String status, LocalDate after);
}
