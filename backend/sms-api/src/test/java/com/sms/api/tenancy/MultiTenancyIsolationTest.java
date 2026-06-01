package com.sms.api.tenancy;

import com.sms.api.entity.*;
import com.sms.api.repository.*;
import com.sms.api.security.JwtService;
import com.sms.api.security.UserPrincipal;
import com.sms.core.enums.FeatureKey;
import com.sms.core.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class MultiTenancyIsolationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired
    MockMvc mockMvc;

    @Autowired
    PlatformRepository platformRepository;
    @Autowired
    SchoolRepository schoolRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    StudentRepository studentRepository;

    @Autowired
    JwtService jwtService;

    private UUID schoolAId;
    private UUID schoolBId;
    private UUID userAId;
    private UUID userBId;

    @BeforeEach
    void setup() {
        // Create schools
        Platform platform = new Platform();
        platform.setName("Test Platform");
        platform.setDomain("tenant-test.local");
        platform = platformRepository.save(platform);

        School schoolA = new School();
        schoolA.setPlatform(platform);
        schoolA.setName("School A");
        schoolA.setCode("SA");
        schoolA.setSubscriptionTier(com.sms.core.enums.SubscriptionTier.FREE);
        schoolA.setEmail("adminA@test.local");
        schoolA = schoolRepository.save(schoolA);
        schoolAId = schoolA.getId();

        School schoolB = new School();
        schoolB.setPlatform(platform);
        schoolB.setName("School B");
        schoolB.setCode("SB");
        schoolB.setSubscriptionTier(com.sms.core.enums.SubscriptionTier.FREE);
        schoolB.setEmail("adminB@test.local");
        schoolB = schoolRepository.save(schoolB);
        schoolBId = schoolB.getId();

        // Create users (school admins)
        User userA = new User();
        userA.setSchool(schoolA);
        userA.setEmail("adminA@test.local");
        userA.setPasswordHash("dummy-hash");
        userA.setRole(Role.SCHOOL_ADMIN);
        userA = userRepository.save(userA);
        userAId = userA.getId();

        User userB = new User();
        userB.setSchool(schoolB);
        userB.setEmail("adminB@test.local");
        userB.setPasswordHash("dummy-hash");
        userB.setRole(Role.SCHOOL_ADMIN);
        userB = userRepository.save(userB);
        userBId = userB.getId();

        // Create students
        Student studentA = new Student();
        studentA.setSchool(schoolA);
        studentA.setAdmissionNo("SA-ADM-001");
        studentA.setRollNo("1");
        studentA.setFirstName("Alice");
        studentA.setLastName("A");
        studentA.setDateOfBirth(LocalDate.of(2010, 1, 1));
        studentA.setGender(com.sms.core.enums.Gender.FEMALE);
        studentA.setCategory(com.sms.core.enums.StudentCategory.GEN);
        studentA.setAdmissionDate(LocalDate.of(2024, 4, 10));
        studentA.setActive(true);
        studentA.setMedicalConditions("{\"notes\":\"A\"}");
        studentRepository.save(studentA);

        Student studentB = new Student();
        studentB.setSchool(schoolB);
        studentB.setAdmissionNo("SB-ADM-001");
        studentB.setRollNo("1");
        studentB.setFirstName("Bob");
        studentB.setLastName("B");
        studentB.setDateOfBirth(LocalDate.of(2010, 2, 2));
        studentB.setGender(com.sms.core.enums.Gender.MALE);
        studentB.setCategory(com.sms.core.enums.StudentCategory.GEN);
        studentB.setAdmissionDate(LocalDate.of(2024, 4, 10));
        studentB.setActive(true);
        studentB.setMedicalConditions("{\"notes\":\"B\"}");
        studentRepository.save(studentB);
    }

    @Test
    void schoolA_canOnlySeeItsOwnStudents() throws Exception {
        String tokenA = jwtService.generateAccessToken(
            userAId,
            schoolAId,
            Role.SCHOOL_ADMIN,
            List.of(FeatureKey.STUDENT_MANAGEMENT),
            "admin-a@test.com"
        );

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/tenancy/debug/students-unsafe")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0]").value("SA-ADM-001"));
    }

    @Test
    void schoolB_canOnlySeeItsOwnStudents() throws Exception {
        String tokenB = jwtService.generateAccessToken(
            userBId,
            schoolBId,
            Role.SCHOOL_ADMIN,
            List.of(FeatureKey.STUDENT_MANAGEMENT),
            "admin-b@test.com"
        );

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/tenancy/debug/students-unsafe")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenB))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0]").value("SB-ADM-001"));
    }
}

