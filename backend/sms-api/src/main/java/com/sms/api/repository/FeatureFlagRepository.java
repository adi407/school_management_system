package com.sms.api.repository;

import com.sms.api.entity.FeatureFlag;
import com.sms.core.enums.FeatureKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FeatureFlagRepository extends JpaRepository<FeatureFlag, UUID> {

    Optional<FeatureFlag> findBySchoolIdAndFeatureKey(UUID schoolId, FeatureKey featureKey);

    List<FeatureFlag> findAllBySchoolId(UUID schoolId);

    @Query("SELECT ff FROM FeatureFlag ff WHERE ff.school.id = :schoolId AND ff.isEnabled = true")
    List<FeatureFlag> findEnabledBySchoolId(UUID schoolId);

    boolean existsBySchoolIdAndFeatureKeyAndIsEnabledTrue(UUID schoolId, FeatureKey featureKey);
}
