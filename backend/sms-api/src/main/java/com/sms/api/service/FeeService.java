package com.sms.api.service;

import com.sms.api.dto.fee.*;
import com.sms.api.entity.*;
import com.sms.api.repository.*;
import com.sms.core.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class FeeService {

    private final FeeStructureRepository structureRepository;
    private final FeePaymentRepository   paymentRepository;
    private final StudentRepository      studentRepository;
    private final SchoolClassRepository  classRepository;
    private final AcademicYearRepository academicYearRepository;
    private final UserRepository         userRepository;

    public FeeService(FeeStructureRepository structureRepository,
                      FeePaymentRepository paymentRepository,
                      StudentRepository studentRepository,
                      SchoolClassRepository classRepository,
                      AcademicYearRepository academicYearRepository,
                      UserRepository userRepository) {
        this.structureRepository    = structureRepository;
        this.paymentRepository      = paymentRepository;
        this.studentRepository      = studentRepository;
        this.classRepository        = classRepository;
        this.academicYearRepository = academicYearRepository;
        this.userRepository         = userRepository;
    }

    // ── Fee Structures ────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<FeeStructureDto> listStructures(UUID schoolId) {
        return structureRepository.findBySchoolIdOrderByFeeType(schoolId)
            .stream().map(this::toStructureDto).toList();
    }

    public FeeStructureDto createStructure(UUID schoolId, CreateFeeStructureRequest req) {
        FeeStructure fs = new FeeStructure();
        fs.setSchoolId(schoolId);
        fs.setFeeType(req.feeType());
        fs.setAmount(req.amount());
        fs.setDueDate(req.dueDate());
        fs.setRecurring(req.isRecurring());
        fs.setFrequency(req.frequency());
        fs.setDescription(req.description());

        if (req.classId() != null) {
            SchoolClass sc = classRepository.findByIdAndSchoolId(req.classId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Class", req.classId()));
            fs.setSchoolClass(sc);
        }

        if (req.academicYearId() != null) {
            AcademicYear ay = academicYearRepository.findById(req.academicYearId())
                .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", req.academicYearId()));
            fs.setAcademicYear(ay);
        }

        return toStructureDto(structureRepository.save(fs));
    }

    public void deleteStructure(UUID schoolId, UUID structureId) {
        FeeStructure fs = structureRepository.findById(structureId)
            .filter(s -> s.getSchoolId().equals(schoolId))
            .orElseThrow(() -> new ResourceNotFoundException("FeeStructure", structureId));
        structureRepository.delete(fs);
    }

    // ── Payments ──────────────────────────────────────────────────────────────

    public FeePaymentDto recordPayment(UUID schoolId, UUID collectedByUserId,
                                       RecordPaymentRequest req) {
        Student student = studentRepository.findByIdAndSchoolId(req.studentId(), schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("Student", req.studentId()));

        User collector = userRepository.findById(collectedByUserId)
            .orElseThrow(() -> new ResourceNotFoundException("User", collectedByUserId));

        FeePayment payment = new FeePayment();
        payment.setSchoolId(schoolId);
        payment.setStudent(student);
        payment.setCollectedBy(collector);
        payment.setAmountPaid(req.amountPaid());
        payment.setPaymentDate(req.paymentDate());
        payment.setPaymentMode(req.paymentMode().toUpperCase());
        payment.setReceiptNo(generateReceiptNo(schoolId));
        payment.setRemarks(req.remarks());

        if (req.feeStructureId() != null) {
            FeeStructure fs = structureRepository.findById(req.feeStructureId())
                .filter(s -> s.getSchoolId().equals(schoolId))
                .orElseThrow(() -> new ResourceNotFoundException("FeeStructure", req.feeStructureId()));
            payment.setFeeStructure(fs);
        }

        return toPaymentDto(paymentRepository.save(payment));
    }

    @Transactional(readOnly = true)
    public List<FeePaymentDto> getPaymentHistory(UUID schoolId, UUID studentId) {
        studentRepository.findByIdAndSchoolId(studentId, schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));
        return paymentRepository.findByStudentIdOrderByPaymentDateDesc(studentId)
            .stream().map(this::toPaymentDto).toList();
    }

    @Transactional(readOnly = true)
    public StudentFeesSummaryDto getStudentFeesSummary(UUID schoolId, UUID studentId) {
        Student student = studentRepository.findByIdAndSchoolId(studentId, schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));

        // Structures applicable to this student (class-specific + school-wide)
        UUID classId = student.getSchoolClass() != null ? student.getSchoolClass().getId() : null;
        List<FeeStructure> structures = classId != null
            ? structureRepository.findBySchoolClassIdOrSchoolClassIsNullAndSchoolId(classId, schoolId)
            : structureRepository.findBySchoolIdOrderByFeeType(schoolId);

        BigDecimal totalFees = structures.stream()
            .map(FeeStructure::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPaid = paymentRepository.sumPaidByStudent(studentId);
        BigDecimal balance   = totalFees.subtract(totalPaid);

        List<FeeStructureDto> structureDtos = structures.stream()
            .map(this::toStructureDto).toList();

        List<FeePaymentDto> paymentDtos = paymentRepository
            .findByStudentIdOrderByPaymentDateDesc(studentId)
            .stream().map(this::toPaymentDto).toList();

        return new StudentFeesSummaryDto(
            studentId,
            student.getFirstName() + " " + student.getLastName(),
            totalFees, totalPaid, balance,
            structureDtos, paymentDtos
        );
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String generateReceiptNo(UUID schoolId) {
        String prefix = "RCP-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + "-";
        String suffix = String.valueOf(System.currentTimeMillis() % 100000);
        String candidate = prefix + suffix;
        // Retry until unique (collision is extremely rare)
        while (paymentRepository.existsByReceiptNo(candidate)) {
            candidate = prefix + (System.currentTimeMillis() % 100000);
        }
        return candidate;
    }

    private FeeStructureDto toStructureDto(FeeStructure fs) {
        SchoolClass sc = fs.getSchoolClass();
        AcademicYear ay = fs.getAcademicYear();
        return new FeeStructureDto(
            fs.getId(),
            sc  != null ? sc.getId()   : null,
            sc  != null ? sc.getName() : null,
            ay  != null ? ay.getId()   : null,
            ay  != null ? ay.getName() : null,
            fs.getFeeType(),
            fs.getAmount(),
            fs.getDueDate(),
            fs.isRecurring(),
            fs.getFrequency(),
            fs.getDescription()
        );
    }

    private FeePaymentDto toPaymentDto(FeePayment fp) {
        Student     s  = fp.getStudent();
        FeeStructure fs = fp.getFeeStructure();
        User        cb  = fp.getCollectedBy();
        return new FeePaymentDto(
            fp.getId(),
            s  != null ? s.getId()              : null,
            s  != null ? s.getFullName()         : null,
            s  != null ? s.getAdmissionNo()      : null,
            fs != null ? fs.getId()              : null,
            fs != null ? fs.getFeeType()         : null,
            fp.getAmountPaid(),
            fp.getPaymentDate(),
            fp.getPaymentMode(),
            fp.getReceiptNo(),
            fp.getRemarks(),
            cb != null ? cb.getId()              : null,
            cb != null ? cb.getEmail()           : null
        );
    }
}
