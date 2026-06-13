package com.sms.api.repository;

import com.sms.api.entity.SchoolRegistration;
import com.sms.api.entity.SchoolRegistration.RegistrationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SchoolRegistrationRepository extends JpaRepository<SchoolRegistration, UUID> {

    Page<SchoolRegistration> findByStatusOrderByCreatedAtDesc(RegistrationStatus status, Pageable pageable);

    Page<SchoolRegistration> findAllByOrderByCreatedAtDesc(Pageable pageable);

    boolean existsByAdminEmail(String adminEmail);

    boolean existsBySchoolCode(String schoolCode);

    long countByStatus(RegistrationStatus status);
}
