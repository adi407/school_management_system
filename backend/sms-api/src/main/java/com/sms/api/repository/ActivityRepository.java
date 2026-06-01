package com.sms.api.repository;

import com.sms.api.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, UUID> {
    List<Activity> findBySchoolIdOrderByNameAsc(UUID schoolId);
    Optional<Activity> findByIdAndSchoolId(UUID id, UUID schoolId);
}
