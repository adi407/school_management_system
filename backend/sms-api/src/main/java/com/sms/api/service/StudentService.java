package com.sms.api.service;

import com.sms.api.dto.student.*;
import com.sms.api.entity.*;
import com.sms.api.repository.*;
import com.sms.core.enums.Gender;
import com.sms.core.enums.StudentCategory;
import com.sms.core.exception.DuplicateResourceException;
import com.sms.core.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class StudentService {

    private final StudentRepository      studentRepository;
    private final GuardianRepository     guardianRepository;
    private final SchoolRepository       schoolRepository;
    private final SchoolClassRepository  classRepository;
    private final AcademicYearRepository academicYearRepository;

    public StudentService(
        StudentRepository studentRepository,
        GuardianRepository guardianRepository,
        SchoolRepository schoolRepository,
        SchoolClassRepository classRepository,
        AcademicYearRepository academicYearRepository
    ) {
        this.studentRepository      = studentRepository;
        this.guardianRepository     = guardianRepository;
        this.schoolRepository       = schoolRepository;
        this.classRepository        = classRepository;
        this.academicYearRepository = academicYearRepository;
    }

    // ── List (paginated / filtered) ───────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<StudentSummaryDto> listStudents(
        UUID schoolId,
        UUID classId,
        Gender gender,
        StudentCategory category,
        Boolean isActive,
        String search,
        Pageable pageable
    ) {
        // Pass "" instead of null so JDBC binds it as varchar; LIKE '%%' matches everything
        String effectiveSearch = (search != null) ? search : "";
        return studentRepository
            .searchStudents(schoolId, classId, gender, category, isActive, effectiveSearch, pageable)
            .map(this::toSummaryDto);
    }

    // ── Get by ID ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public StudentDto getStudent(UUID schoolId, UUID studentId) {
        Student student = findOrThrow(schoolId, studentId);
        List<Guardian> guardians = guardianRepository.findByStudentIdOrderByIsPrimaryDesc(studentId);
        return toDto(student, guardians);
    }

    // ── Create ───────────────────────────────────────────────────────────────

    public StudentDto createStudent(UUID schoolId, CreateStudentRequest req) {
        School school = schoolRepository.findById(schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("School", schoolId));

        // Resolve admission number
        String admissionNo = resolveAdmissionNo(req.admissionNo(), school, schoolId);

        if (studentRepository.existsBySchoolIdAndAdmissionNo(schoolId, admissionNo)) {
            throw new DuplicateResourceException(
                "Admission number '" + admissionNo + "' already exists in this school");
        }

        // Resolve optional class
        SchoolClass schoolClass = null;
        if (req.classId() != null) {
            schoolClass = classRepository.findByIdAndSchoolId(req.classId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Class not found or does not belong to this school: " + req.classId()));
        }

        // Resolve optional academic year (default to current)
        AcademicYear academicYear = resolveAcademicYear(req.academicYearId(), schoolId);

        // Build student
        Student student = new Student();
        student.setSchool(school);
        student.setAdmissionNo(admissionNo);
        student.setRollNo(req.rollNo());
        student.setFirstName(req.firstName().trim());
        student.setLastName(req.lastName().trim());
        student.setDateOfBirth(req.dateOfBirth());
        student.setGender(req.gender());
        student.setAdmissionDate(req.admissionDate());
        student.setSchoolClass(schoolClass);
        student.setAcademicYear(academicYear);
        student.setBloodGroup(req.bloodGroup());
        student.setNationality(req.nationality() != null ? req.nationality() : "Indian");
        student.setReligion(req.religion());
        student.setCaste(req.caste());
        student.setCategory(req.category() != null ? req.category() : StudentCategory.GEN);
        student.setMotherTongue(req.motherTongue());
        student.setAadhaarNo(req.aadhaarNo());
        student.setHouseGroup(req.houseGroup());
        student.setMedicalConditions(req.medicalConditions());
        student.setPhotoUrl(req.photoUrl());
        student = studentRepository.save(student);

        // Build guardians
        List<Guardian> guardians = buildAndSaveGuardians(req.guardians(), student, schoolId);

        return toDto(student, guardians);
    }

    // ── Update ───────────────────────────────────────────────────────────────

    public StudentDto updateStudent(UUID schoolId, UUID studentId, UpdateStudentRequest req) {
        Student student = findOrThrow(schoolId, studentId);

        if (req.firstName()    != null) student.setFirstName(req.firstName().trim());
        if (req.lastName()     != null) student.setLastName(req.lastName().trim());
        if (req.dateOfBirth()  != null) student.setDateOfBirth(req.dateOfBirth());
        if (req.gender()       != null) student.setGender(req.gender());
        if (req.bloodGroup()   != null) student.setBloodGroup(req.bloodGroup());
        if (req.nationality()  != null) student.setNationality(req.nationality());
        if (req.religion()     != null) student.setReligion(req.religion());
        if (req.caste()        != null) student.setCaste(req.caste());
        if (req.category()     != null) student.setCategory(req.category());
        if (req.motherTongue() != null) student.setMotherTongue(req.motherTongue());
        if (req.aadhaarNo()    != null) student.setAadhaarNo(req.aadhaarNo());
        if (req.rollNo()       != null) student.setRollNo(req.rollNo());
        if (req.houseGroup()   != null) student.setHouseGroup(req.houseGroup());
        if (req.medicalConditions() != null) student.setMedicalConditions(req.medicalConditions());
        if (req.photoUrl()     != null) student.setPhotoUrl(req.photoUrl());

        if (req.classId() != null) {
            SchoolClass sc = classRepository.findByIdAndSchoolId(req.classId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Class not found or does not belong to this school: " + req.classId()));
            student.setSchoolClass(sc);
        }
        if (req.academicYearId() != null) {
            AcademicYear ay = academicYearRepository.findByIdAndSchoolId(req.academicYearId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Academic year not found: " + req.academicYearId()));
            student.setAcademicYear(ay);
        }

        student = studentRepository.save(student);
        List<Guardian> guardians = guardianRepository.findByStudentIdOrderByIsPrimaryDesc(studentId);
        return toDto(student, guardians);
    }

    // ── Activate / Deactivate ─────────────────────────────────────────────────

    public void setActive(UUID schoolId, UUID studentId, boolean active) {
        Student student = findOrThrow(schoolId, studentId);
        student.setActive(active);
        studentRepository.save(student);
    }

    // ── Guardian management ───────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<GuardianDto> getGuardians(UUID schoolId, UUID studentId) {
        findOrThrow(schoolId, studentId); // security check
        return guardianRepository.findByStudentIdOrderByIsPrimaryDesc(studentId)
            .stream().map(this::toGuardianDto).toList();
    }

    public GuardianDto addGuardian(UUID schoolId, UUID studentId, CreateGuardianRequest req) {
        Student student = findOrThrow(schoolId, studentId);
        Guardian g = buildGuardian(req, student, schoolId);
        return toGuardianDto(guardianRepository.save(g));
    }

    public GuardianDto updateGuardian(UUID schoolId, UUID studentId,
                                      UUID guardianId, CreateGuardianRequest req) {
        findOrThrow(schoolId, studentId); // security check
        Guardian g = guardianRepository.findByIdAndStudentId(guardianId, studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Guardian", guardianId));

        g.setName(req.name());
        g.setRelation(req.relation());
        g.setPhone(req.phone());
        if (req.email()      != null) g.setEmail(req.email());
        if (req.aadhaarNo()  != null) g.setAadhaarNo(req.aadhaarNo());
        if (req.occupation() != null) g.setOccupation(req.occupation());
        if (req.address()    != null) g.setAddress(req.address());
        g.setPrimary(req.isPrimary());
        g.setAuthorizedPickup(req.isAuthorizedPickup());
        return toGuardianDto(guardianRepository.save(g));
    }

    public void deleteGuardian(UUID schoolId, UUID studentId, UUID guardianId) {
        findOrThrow(schoolId, studentId); // security check
        Guardian g = guardianRepository.findByIdAndStudentId(guardianId, studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Guardian", guardianId));
        guardianRepository.delete(g);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Student findOrThrow(UUID schoolId, UUID studentId) {
        return studentRepository.findByIdAndSchoolId(studentId, schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));
    }

    private String resolveAdmissionNo(String requested, School school, UUID schoolId) {
        if (requested != null && !requested.isBlank()) {
            return requested.trim().toUpperCase();
        }
        // Auto-generate: {CODE}/{YEAR}/{SEQ:04d}
        int year = LocalDate.now().getYear();
        long seq = studentRepository.countBySchoolId(schoolId) + 1;
        return String.format("%s/%d/%04d", school.getCode(), year, seq);
    }

    private AcademicYear resolveAcademicYear(UUID requestedId, UUID schoolId) {
        if (requestedId != null) {
            return academicYearRepository.findByIdAndSchoolId(requestedId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year", requestedId));
        }
        // Default to current year (may be null if none seeded)
        return academicYearRepository.findBySchoolIdAndIsCurrentTrue(schoolId).orElse(null);
    }

    private List<Guardian> buildAndSaveGuardians(
        List<CreateGuardianRequest> reqs, Student student, UUID schoolId
    ) {
        List<Guardian> list = reqs.stream()
            .map(r -> buildGuardian(r, student, schoolId))
            .toList();
        return guardianRepository.saveAll(list);
    }

    private Guardian buildGuardian(CreateGuardianRequest req, Student student, UUID schoolId) {
        Guardian g = new Guardian();
        g.setStudent(student);
        g.setSchoolId(schoolId);
        g.setName(req.name().trim());
        g.setRelation(req.relation().trim());
        g.setPhone(req.phone().trim());
        g.setEmail(req.email());
        g.setAadhaarNo(req.aadhaarNo());
        g.setOccupation(req.occupation());
        g.setAddress(req.address());
        g.setPrimary(req.isPrimary());
        g.setAuthorizedPickup(req.isAuthorizedPickup());
        return g;
    }

    // ── DTO mappers ───────────────────────────────────────────────────────────

    private StudentSummaryDto toSummaryDto(Student s) {
        return new StudentSummaryDto(
            s.getId(),
            s.getAdmissionNo(),
            s.getRollNo(),
            s.getFirstName(),
            s.getLastName(),
            s.getFullName(),
            s.getDateOfBirth(),
            s.getGender(),
            s.getCategory(),
            s.getSchoolClass() != null ? s.getSchoolClass().getId()   : null,
            s.getSchoolClass() != null ? s.getSchoolClass().getName() : null,
            s.isActive(),
            s.getAdmissionDate(),
            s.getPhotoUrl(),
            s.getCreatedAt()
        );
    }

    private StudentDto toDto(Student s, List<Guardian> guardians) {
        SchoolClass  sc = s.getSchoolClass();
        AcademicYear ay = s.getAcademicYear();

        return new StudentDto(
            s.getId(),
            s.getSchoolId(),
            s.getAdmissionNo(),
            s.getRollNo(),
            s.getFirstName(),
            s.getLastName(),
            s.getFullName(),
            s.getDateOfBirth(),
            s.getGender(),
            s.getBloodGroup(),
            s.getNationality(),
            s.getReligion(),
            s.getCaste(),
            s.getCategory(),
            s.getMotherTongue(),
            s.getAadhaarNo(),
            sc != null ? sc.getId()   : null,
            sc != null ? sc.getName() : null,
            ay != null ? ay.getId()   : null,
            ay != null ? ay.getName() : null,
            s.getHouseGroup(),
            s.isActive(),
            s.getAdmissionDate(),
            s.isTcIssued(),
            s.getPhotoUrl(),
            s.getMedicalConditions(),
            guardians.stream().map(this::toGuardianDto).toList(),
            s.getCreatedAt(),
            s.getUpdatedAt()
        );
    }

    private GuardianDto toGuardianDto(Guardian g) {
        return new GuardianDto(
            g.getId(),
            g.getName(),
            g.getRelation(),
            g.getPhone(),
            g.getEmail(),
            g.getAadhaarNo(),
            g.getOccupation(),
            g.getAddress(),
            g.isPrimary(),
            g.isAuthorizedPickup(),
            g.getPhotoUrl()
        );
    }
}
