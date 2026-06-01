package com.sms.api.service;

import com.sms.api.dto.homework.GradeSubmissionRequest;
import com.sms.api.dto.homework.HomeworkSubmissionDto;
import com.sms.api.dto.homework.SubmitHomeworkRequest;
import com.sms.api.entity.Homework;
import com.sms.api.entity.HomeworkSubmission;
import com.sms.api.entity.Student;
import com.sms.api.entity.User;
import com.sms.api.repository.HomeworkRepository;
import com.sms.api.repository.HomeworkSubmissionRepository;
import com.sms.api.repository.StudentRepository;
import com.sms.api.repository.UserRepository;
import com.sms.core.exception.DuplicateResourceException;
import com.sms.core.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class HomeworkSubmissionService {

    private final HomeworkSubmissionRepository submissionRepository;
    private final HomeworkRepository           homeworkRepository;
    private final StudentRepository            studentRepository;
    private final UserRepository               userRepository;

    public HomeworkSubmissionService(HomeworkSubmissionRepository submissionRepository,
                                     HomeworkRepository homeworkRepository,
                                     StudentRepository studentRepository,
                                     UserRepository userRepository) {
        this.submissionRepository = submissionRepository;
        this.homeworkRepository   = homeworkRepository;
        this.studentRepository    = studentRepository;
        this.userRepository       = userRepository;
    }

    // ── Student: submit homework ──────────────────────────────────────────────

    public HomeworkSubmissionDto submit(UUID schoolId, UUID homeworkId,
                                        UUID studentId, SubmitHomeworkRequest req) {
        Homework hw = homeworkRepository.findByIdAndSchoolId(homeworkId, schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("Homework", homeworkId));

        if (!hw.isPublished()) {
            throw new IllegalStateException("Homework is not published yet");
        }

        Student student = studentRepository.findByIdAndSchoolId(studentId, schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));

        // Prevent duplicate submission
        if (submissionRepository.existsByHomeworkIdAndStudentId(homeworkId, studentId)) {
            throw new DuplicateResourceException("You have already submitted this homework");
        }

        HomeworkSubmission sub = new HomeworkSubmission();
        sub.setHomework(hw);
        sub.setStudent(student);
        sub.setSchoolId(schoolId);
        sub.setContent(req.content());
        sub.setSubmittedAt(Instant.now());

        if (req.attachments() != null && !req.attachments().isBlank()) {
            sub.setAttachments(req.attachments());
        }

        // Determine status: LATE if past due date, SUBMITTED otherwise
        boolean isLate = LocalDate.now().isAfter(hw.getDueDate());
        sub.setStatus(isLate ? "LATE" : "SUBMITTED");

        return toDto(submissionRepository.save(sub));
    }

    // ── Teacher: grade a submission ───────────────────────────────────────────

    public HomeworkSubmissionDto grade(UUID schoolId, UUID homeworkId,
                                       UUID studentId, UUID teacherUserId,
                                       GradeSubmissionRequest req) {
        // Verify homework belongs to this school
        homeworkRepository.findByIdAndSchoolId(homeworkId, schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("Homework", homeworkId));

        HomeworkSubmission sub = submissionRepository
            .findByHomeworkIdAndStudentId(homeworkId, studentId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Submission for student " + studentId + " on homework " + homeworkId));

        User teacher = userRepository.findById(teacherUserId)
            .orElseThrow(() -> new ResourceNotFoundException("User", teacherUserId));

        sub.setGrade(req.grade());
        sub.setRemarks(req.remarks());
        sub.setGradedBy(teacher);
        sub.setGradedAt(Instant.now());
        sub.setStatus("GRADED");

        return toDto(submissionRepository.save(sub));
    }

    // ── Teacher: list all submissions for a homework ──────────────────────────

    @Transactional(readOnly = true)
    public List<HomeworkSubmissionDto> getSubmissions(UUID schoolId, UUID homeworkId) {
        homeworkRepository.findByIdAndSchoolId(homeworkId, schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("Homework", homeworkId));

        return submissionRepository
            .findByHomeworkIdOrderBySubmittedAtAsc(homeworkId)
            .stream().map(this::toDto).toList();
    }

    // ── Student: fetch own submission for a homework ──────────────────────────

    @Transactional(readOnly = true)
    public HomeworkSubmissionDto getMySubmission(UUID schoolId, UUID homeworkId, UUID studentId) {
        homeworkRepository.findByIdAndSchoolId(homeworkId, schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("Homework", homeworkId));

        return submissionRepository
            .findByHomeworkIdAndStudentId(homeworkId, studentId)
            .map(this::toDto)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Submission for homework " + homeworkId));
    }

    // ── Student / Parent: submission history ─────────────────────────────────

    @Transactional(readOnly = true)
    public List<HomeworkSubmissionDto> getStudentHistory(UUID schoolId, UUID studentId) {
        return submissionRepository
            .findBySchoolIdAndStudentIdOrderByCreatedAtDesc(schoolId, studentId)
            .stream().map(this::toDto).toList();
    }

    // ── Summary counts for a homework (pending / submitted / graded) ──────────

    @Transactional(readOnly = true)
    public java.util.Map<String, Long> getSummary(UUID schoolId, UUID homeworkId) {
        homeworkRepository.findByIdAndSchoolId(homeworkId, schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("Homework", homeworkId));

        return java.util.Map.of(
            "PENDING",   submissionRepository.countByHomeworkIdAndStatus(homeworkId, "PENDING"),
            "SUBMITTED", submissionRepository.countByHomeworkIdAndStatus(homeworkId, "SUBMITTED"),
            "LATE",      submissionRepository.countByHomeworkIdAndStatus(homeworkId, "LATE"),
            "GRADED",    submissionRepository.countByHomeworkIdAndStatus(homeworkId, "GRADED")
        );
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private HomeworkSubmissionDto toDto(HomeworkSubmission s) {
        Student student = s.getStudent();
        Homework hw     = s.getHomework();
        User gradedBy   = s.getGradedBy();

        return new HomeworkSubmissionDto(
            s.getId(),
            hw != null  ? hw.getId()    : null,
            hw != null  ? hw.getTitle() : null,
            student != null ? student.getId()        : null,
            student != null ? student.getFirstName() + " " + student.getLastName() : null,
            student != null ? student.getAdmissionNo() : null,
            s.getSchoolId(),
            s.getStatus(),
            s.getContent(),
            s.getAttachments(),
            s.getRemarks(),
            s.getGrade(),
            gradedBy != null ? gradedBy.getId()    : null,
            gradedBy != null ? gradedBy.getEmail() : null,
            s.getSubmittedAt(),
            s.getGradedAt(),
            s.getCreatedAt()
        );
    }
}
