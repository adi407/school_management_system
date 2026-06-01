package com.sms.api.tenancy;

import com.sms.api.entity.Student;
import jakarta.persistence.EntityManager;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import com.sms.api.security.UserPrincipal;

@RestController
@RequestMapping("/api/v1/tenancy/debug")
public class TenantLeakTestController {

    private final EntityManager entityManager;

    public TenantLeakTestController(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Executes an intentionally "unsafe" JPQL query without any explicit schoolId predicate.
     * If the Hibernate {@code schoolFilter} is correctly enabled from the JWT, results must
     * be automatically restricted to the current tenant.
     *
     * This endpoint exists only in test sources (src/test/java), so it is not part of production.
     */
    @GetMapping("/students-unsafe")
    public List<String> studentsUnsafe(@AuthenticationPrincipal UserPrincipal principal) {
        // principal is unused on purpose: we want to prove the filter does the isolation work.
        return entityManager
            .createQuery("SELECT s FROM Student s", Student.class)
            .getResultList()
            .stream()
            .map(Student::getAdmissionNo)
            .toList();
    }
}

