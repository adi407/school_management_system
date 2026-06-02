package com.sms.api.repository;

import com.sms.api.entity.School;
import com.sms.core.enums.SubscriptionTier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SchoolRepository extends JpaRepository<School, UUID> {

    Optional<School> findByCode(String code);

    boolean existsByCode(String code);

    Page<School> findAllByIsActive(boolean isActive, Pageable pageable);

    @Query(value = "SELECT * FROM schools s WHERE " +
           "(:search IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:tier IS NULL OR s.subscription_tier = :tier) AND " +
           "(:isActive IS NULL OR s.is_active = :isActive)",
           countQuery = "SELECT COUNT(*) FROM schools s WHERE " +
           "(:search IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:tier IS NULL OR s.subscription_tier = :tier) AND " +
           "(:isActive IS NULL OR s.is_active = :isActive)",
           nativeQuery = true)
    Page<School> searchSchoolsNative(String search, String tier, Boolean isActive, Pageable pageable);

    @Query("SELECT s FROM School s WHERE s.subscriptionExpiry < :date AND s.isActive = true")
    List<School> findExpiringSoon(LocalDate date);

    long countByIsActive(boolean isActive);

    long countBySubscriptionTier(SubscriptionTier tier);
}
