package com.sms.api.repository;

import com.sms.api.entity.DemoRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DemoRequestRepository extends JpaRepository<DemoRequest, UUID> {}
