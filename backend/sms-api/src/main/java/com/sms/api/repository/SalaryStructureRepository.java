package com.sms.api.repository;

import com.sms.api.entity.SalaryStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SalaryStructureRepository extends JpaRepository<SalaryStructure, UUID> {

    List<SalaryStructure> findBySchoolIdOrderByCreatedAtDesc(UUID schoolId);

    List<SalaryStructure> findByStaffIdAndSchoolId(UUID staffId, UUID schoolId);

    /** Active structure effective on or before a given date for a staff member */
    @Query("SELECT s FROM SalaryStructure s " +
           "WHERE s.staff.id = :staffId " +
           "AND s.schoolId = :schoolId " +
           "AND s.isActive = true " +
           "AND s.effectiveFrom <= :onDate " +
           "AND (s.effectiveTo IS NULL OR s.effectiveTo >= :onDate) " +
           "ORDER BY s.effectiveFrom DESC")
    List<SalaryStructure> findActiveForStaffOnDate(@Param("staffId") UUID staffId,
                                                    @Param("schoolId") UUID schoolId,
                                                    @Param("onDate") LocalDate onDate);

    /** All active structures for the school — used when triggering a payroll run */
    @Query("SELECT s FROM SalaryStructure s " +
           "WHERE s.schoolId = :schoolId " +
           "AND s.isActive = true " +
           "AND s.effectiveFrom <= :onDate " +
           "AND (s.effectiveTo IS NULL OR s.effectiveTo >= :onDate)")
    List<SalaryStructure> findAllActiveForSchoolOnDate(@Param("schoolId") UUID schoolId,
                                                        @Param("onDate") LocalDate onDate);

    Optional<SalaryStructure> findByIdAndSchoolId(UUID id, UUID schoolId);
}
