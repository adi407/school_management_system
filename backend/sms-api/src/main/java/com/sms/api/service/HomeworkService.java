package com.sms.api.service;

import com.sms.api.dto.homework.CreateHomeworkRequest;
import com.sms.api.dto.homework.HomeworkDto;
import com.sms.api.entity.*;
import com.sms.api.repository.*;
import com.sms.core.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class HomeworkService {

    private final HomeworkRepository     homeworkRepository;
    private final SchoolClassRepository  classRepository;
    private final SubjectRepository      subjectRepository;
    private final AcademicYearRepository academicYearRepository;
    private final SchoolRepository       schoolRepository;
    private final UserRepository         userRepository;

    public HomeworkService(HomeworkRepository homeworkRepository,
                           SchoolClassRepository classRepository,
                           SubjectRepository subjectRepository,
                           AcademicYearRepository academicYearRepository,
                           SchoolRepository schoolRepository,
                           UserRepository userRepository) {
        this.homeworkRepository    = homeworkRepository;
        this.classRepository       = classRepository;
        this.subjectRepository     = subjectRepository;
        this.academicYearRepository = academicYearRepository;
        this.schoolRepository      = schoolRepository;
        this.userRepository        = userRepository;
    }

    // ── Teacher / Admin: paginated, filtered list ─────────────────────────────

    @Transactional(readOnly = true)
    public Page<HomeworkDto> list(UUID schoolId, UUID classId, UUID subjectId,
                                  LocalDate from, LocalDate to, Pageable pageable) {
        return homeworkRepository
            .findFiltered(schoolId, classId, subjectId, from, to, pageable)
            .map(this::toDto);
    }

    // ── Teacher / Admin: single homework ─────────────────────────────────────

    @Transactional(readOnly = true)
    public HomeworkDto get(UUID schoolId, UUID id) {
        return toDto(findOrThrow(schoolId, id));
    }

    // ── Parent / Student: upcoming homework for their class ───────────────────

    @Transactional(readOnly = true)
    public List<HomeworkDto> getUpcoming(UUID classId) {
        return homeworkRepository
            .findUpcomingForClass(classId, LocalDate.now())
            .stream().map(this::toDto).toList();
    }

    // ── Parent / Student: homework in a date range ────────────────────────────

    @Transactional(readOnly = true)
    public List<HomeworkDto> getForClassInRange(UUID classId, LocalDate from, LocalDate to) {
        return homeworkRepository
            .findForClassInRange(classId, from, to)
            .stream().map(this::toDto).toList();
    }

    // ── Create ────────────────────────────────────────────────────────────────

    public HomeworkDto create(UUID schoolId, UUID teacherUserId, CreateHomeworkRequest req) {
        School school = schoolRepository.findById(schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("School", schoolId));

        User teacher = userRepository.findById(teacherUserId)
            .orElseThrow(() -> new ResourceNotFoundException("User", teacherUserId));

        Homework hw = new Homework();
        hw.setSchool(school);
        hw.setTeacher(teacher);
        hw.setSchoolWide(req.isSchoolWide());

        // null classId = school-wide broadcast; otherwise scope to a specific class
        if (!req.isSchoolWide() && req.classId() != null) {
            SchoolClass schoolClass = classRepository.findByIdAndSchoolId(req.classId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Class", req.classId()));
            hw.setSchoolClass(schoolClass);
        }
        hw.setTitle(req.title());
        hw.setDescription(req.description());
        hw.setDueDate(req.dueDate());
        hw.setPublished(req.isPublished());

        if (req.estimatedMinutes() != null) hw.setEstimatedMinutes(req.estimatedMinutes());

        if (req.subjectId() != null) {
            Subject subject = subjectRepository.findByIdAndSchoolId(req.subjectId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", req.subjectId()));
            hw.setSubject(subject);
        }

        if (req.academicYearId() != null) {
            AcademicYear ay = academicYearRepository.findByIdAndSchoolId(req.academicYearId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", req.academicYearId()));
            hw.setAcademicYear(ay);
        }

        return toDto(homeworkRepository.save(hw));
    }

    // ── Update ────────────────────────────────────────────────────────────────

    public HomeworkDto update(UUID schoolId, UUID id, CreateHomeworkRequest req) {
        Homework hw = findOrThrow(schoolId, id);

        if (req.title()            != null) hw.setTitle(req.title());
        if (req.description()      != null) hw.setDescription(req.description());
        if (req.dueDate()          != null) hw.setDueDate(req.dueDate());
        if (req.estimatedMinutes() != null) hw.setEstimatedMinutes(req.estimatedMinutes());

        // Allow toggling published state
        hw.setPublished(req.isPublished());
        hw.setSchoolWide(req.isSchoolWide());

        if (req.subjectId() != null) {
            Subject subject = subjectRepository.findByIdAndSchoolId(req.subjectId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", req.subjectId()));
            hw.setSubject(subject);
        }

        return toDto(homeworkRepository.save(hw));
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    public void delete(UUID schoolId, UUID id) {
        homeworkRepository.delete(findOrThrow(schoolId, id));
    }

    // ── Publish / Unpublish ───────────────────────────────────────────────────

    public HomeworkDto setPublished(UUID schoolId, UUID id, boolean published) {
        Homework hw = findOrThrow(schoolId, id);
        hw.setPublished(published);
        return toDto(homeworkRepository.save(hw));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Homework findOrThrow(UUID schoolId, UUID id) {
        return homeworkRepository.findByIdAndSchoolId(id, schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("Homework", id));
    }

    private HomeworkDto toDto(Homework hw) {
        SchoolClass sc      = hw.getSchoolClass();
        Subject     subject = hw.getSubject();
        User        teacher = hw.getTeacher();
        long daysUntilDue   = ChronoUnit.DAYS.between(LocalDate.now(), hw.getDueDate());

        return new HomeworkDto(
            hw.getId(),
            hw.getSchoolId(),
            sc != null ? sc.getId()   : null,
            sc != null ? sc.getName() : null,
            subject != null ? subject.getId()   : null,
            subject != null ? subject.getName() : null,
            teacher != null ? teacher.getId()    : null,
            teacher != null ? teacher.getEmail() : null,
            hw.getTitle(),
            hw.getDescription(),
            hw.getDueDate(),
            hw.getEstimatedMinutes(),
            hw.getAttachments(),
            hw.isPublished(),
            hw.isSchoolWide(),
            daysUntilDue,
            hw.getCreatedAt(),
            hw.getUpdatedAt()
        );
    }
}
