package com.sms.api.repository;

import com.sms.api.entity.Platform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlatformRepository extends JpaRepository<Platform, UUID> {
    Optional<Platform> findByDomain(String domain);
}
