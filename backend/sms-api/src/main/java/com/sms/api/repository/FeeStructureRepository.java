package com.sms.api.repository;

import com.sms.api.entity.FeeStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FeeStructureRepository extends JpaRepository<FeeStructure, UUID> {
    List<FeeStructure> findBySchoolIdOrderByFeeType(UUID schoolId);
    List<FeeStructure> findBySchoolClassIdOrSchoolClassIsNullAndSchoolId(UUID classId, UUID schoolId);

    @org.springframework.data.jpa.repository.Query(
        "SELECT COALESCE(SUM(f.amount), 0) FROM FeeStructure f WHERE f.schoolId = :schoolId")
    java.math.BigDecimal sumTotalFeesBySchool(@org.springframework.data.repository.query.Param("schoolId") UUID schoolId);
}
