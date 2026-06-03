package com.sms.api.repository;

import com.sms.api.entity.StaffModuleAssignment;
import com.sms.core.enums.StaffModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StaffModuleAssignmentRepository extends JpaRepository<StaffModuleAssignment, UUID> {

    /** All active module assignments for a staff member — used to build sidebar */
    List<StaffModuleAssignment> findByStaffIdAndSchoolIdAndIsActiveTrue(UUID staffId, UUID schoolId);

    /** All active assignments for a module across the school (who has access?) */
    List<StaffModuleAssignment> findBySchoolIdAndModuleAndIsActiveTrue(UUID schoolId, StaffModule module);

    /** All assignments for a specific staff member (active + inactive) */
    List<StaffModuleAssignment> findByStaffIdAndSchoolId(UUID staffId, UUID schoolId);

    /** Check if a staff member holds a specific module (active only) */
    Optional<StaffModuleAssignment> findByStaffIdAndSchoolIdAndModuleAndIsActiveTrue(
        UUID staffId, UUID schoolId, StaffModule module);

    /** Find any row for staff+module regardless of active state — for upsert logic */
    Optional<StaffModuleAssignment> findByStaffIdAndSchoolIdAndModule(
        UUID staffId, UUID schoolId, StaffModule module);

    /** Count active assignments across the whole school — useful for dashboard stats */
    @Query("SELECT COUNT(DISTINCT a.staff.id) FROM StaffModuleAssignment a " +
           "WHERE a.schoolId = :schoolId AND a.isActive = true")
    long countDistinctStaffWithAnyModule(@Param("schoolId") UUID schoolId);

    /** All assignments grouped by staff — for the admin assignment page */
    @Query("SELECT a FROM StaffModuleAssignment a " +
           "WHERE a.schoolId = :schoolId AND a.isActive = true " +
           "ORDER BY a.staff.id, a.module")
    List<StaffModuleAssignment> findAllActiveBySchool(@Param("schoolId") UUID schoolId);
}
