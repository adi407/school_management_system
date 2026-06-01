package com.sms.api.repository;

import com.sms.api.entity.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SchoolClassRepository extends JpaRepository<SchoolClass, UUID> {

    List<SchoolClass> findBySchoolIdOrderByGradeAscSectionAsc(UUID schoolId);

    Optional<SchoolClass> findByIdAndSchoolId(UUID id, UUID schoolId);

    boolean existsBySchoolIdAndGradeAndSection(UUID schoolId, int grade, String section);

    long countBySchoolId(UUID schoolId);
}
