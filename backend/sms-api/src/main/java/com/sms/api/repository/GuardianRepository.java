package com.sms.api.repository;

import com.sms.api.entity.Guardian;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GuardianRepository extends JpaRepository<Guardian, UUID> {

    List<Guardian> findByStudentIdOrderByIsPrimaryDesc(UUID studentId);

    Optional<Guardian> findByIdAndStudentId(UUID id, UUID studentId);

    void deleteByStudentId(UUID studentId);

    /** Find all guardian records matching this email in the school — primary child first */
    List<Guardian> findByEmailAndSchoolIdOrderByIsPrimaryDesc(String email, UUID schoolId);
}
