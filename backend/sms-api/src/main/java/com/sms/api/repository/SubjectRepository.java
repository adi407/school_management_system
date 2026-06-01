package com.sms.api.repository;

import com.sms.api.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, UUID> {

    List<Subject> findBySchoolIdOrderByNameAsc(UUID schoolId);

    Optional<Subject> findByIdAndSchoolId(UUID id, UUID schoolId);

    boolean existsBySchoolIdAndCode(UUID schoolId, String code);
}
