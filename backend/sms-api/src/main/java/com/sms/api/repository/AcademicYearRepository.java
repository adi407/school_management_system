package com.sms.api.repository;

import com.sms.api.entity.AcademicYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AcademicYearRepository extends JpaRepository<AcademicYear, UUID> {

    Optional<AcademicYear> findBySchoolIdAndIsCurrentTrue(UUID schoolId);

    Optional<AcademicYear> findByIdAndSchoolId(UUID id, UUID schoolId);

    List<AcademicYear> findBySchoolIdOrderByStartDateDesc(UUID schoolId);
}
