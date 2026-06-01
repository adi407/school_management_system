package com.sms.api.service;

import com.sms.api.dto.exam.CreateExamRequest;
import com.sms.api.dto.exam.ExamDto;
import com.sms.api.entity.*;
import com.sms.api.repository.*;
import com.sms.core.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ExamService {

    private final ExamRepository examRepository;
    private final AcademicYearRepository yearRepository;
    private final SchoolClassRepository classRepository;

    public ExamService(ExamRepository examRepository,
                       AcademicYearRepository yearRepository,
                       SchoolClassRepository classRepository) {
        this.examRepository = examRepository;
        this.yearRepository = yearRepository;
        this.classRepository = classRepository;
    }

    @Transactional(readOnly = true)
    public List<ExamDto> list(UUID schoolId) {
        return examRepository.findBySchoolIdOrderByStartDateDesc(schoolId)
            .stream().map(this::toDto).toList();
    }

    public ExamDto create(UUID schoolId, CreateExamRequest req) {
        Exam exam = new Exam();
        exam.setSchoolId(schoolId);
        exam.setName(req.name());
        exam.setExamType(req.examType());
        exam.setStartDate(req.startDate());
        exam.setEndDate(req.endDate());
        exam.setTotalSubjects(req.totalSubjects());
        exam.setDescription(req.description());
        exam.setStatus(computeStatus(req.startDate(), req.endDate(), req.status()));

        if (req.academicYearId() != null) {
            AcademicYear ay = yearRepository.findByIdAndSchoolId(req.academicYearId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", req.academicYearId()));
            exam.setAcademicYear(ay);
        }
        if (req.classId() != null) {
            SchoolClass sc = classRepository.findByIdAndSchoolId(req.classId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Class", req.classId()));
            exam.setSchoolClass(sc);
        }
        return toDto(examRepository.save(exam));
    }

    public ExamDto update(UUID schoolId, UUID examId, CreateExamRequest req) {
        Exam exam = examRepository.findByIdAndSchoolId(examId, schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("Exam", examId));
        exam.setName(req.name());
        exam.setExamType(req.examType());
        exam.setStartDate(req.startDate());
        exam.setEndDate(req.endDate());
        exam.setTotalSubjects(req.totalSubjects());
        exam.setDescription(req.description());
        exam.setStatus(computeStatus(req.startDate(), req.endDate(), req.status()));

        if (req.classId() != null) {
            SchoolClass sc = classRepository.findByIdAndSchoolId(req.classId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Class", req.classId()));
            exam.setSchoolClass(sc);
        } else {
            exam.setSchoolClass(null);
        }
        return toDto(examRepository.save(exam));
    }

    public void delete(UUID schoolId, UUID examId) {
        Exam exam = examRepository.findByIdAndSchoolId(examId, schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("Exam", examId));
        examRepository.delete(exam);
    }

    private String computeStatus(LocalDate start, LocalDate end, String requested) {
        if (requested != null && !requested.isBlank()) return requested;
        LocalDate today = LocalDate.now();
        if (today.isBefore(start))  return "UPCOMING";
        if (today.isAfter(end))     return "COMPLETED";
        return "ONGOING";
    }

    private ExamDto toDto(Exam e) {
        AcademicYear ay = e.getAcademicYear();
        SchoolClass  sc = e.getSchoolClass();
        return new ExamDto(
            e.getId(), e.getSchoolId(),
            ay != null ? ay.getId()   : null,
            ay != null ? ay.getName() : null,
            sc != null ? sc.getId()   : null,
            sc != null ? sc.getName() : "All Classes",
            e.getName(), e.getExamType(),
            e.getStartDate(), e.getEndDate(),
            e.getTotalSubjects(), e.getDescription(),
            e.getStatus(), e.getCreatedAt()
        );
    }
}
