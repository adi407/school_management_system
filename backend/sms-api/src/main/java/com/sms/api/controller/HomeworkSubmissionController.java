package com.sms.api.controller;

import com.sms.api.dto.homework.GradeSubmissionRequest;
import com.sms.api.dto.homework.HomeworkSubmissionDto;
import com.sms.api.dto.homework.SubmitHomeworkRequest;
import com.sms.api.entity.Student;
import com.sms.api.repository.StudentRepository;
import com.sms.api.security.UserPrincipal;
import com.sms.api.service.HomeworkSubmissionService;
import com.sms.core.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Homework submission lifecycle — submit → grade → view.
 *
 * POST   /api/v1/homework/{homeworkId}/submissions           Student submits answer
 * POST   /api/v1/homework/{homeworkId}/submissions/{studentId}/grade  Teacher grades
 * GET    /api/v1/homework/{homeworkId}/submissions           Teacher: all submissions
 * GET    /api/v1/homework/{homeworkId}/submissions/me        Student: own submission
 * GET    /api/v1/homework/{homeworkId}/submissions/summary   Teacher: status counts
 * GET    /api/v1/homework/students/{studentId}/history       Admin/Teacher: student history
 */
@RestController
@RequestMapping("/api/v1/homework")
@PreAuthorize("isAuthenticated()")
@Tag(name = "Homework Submissions", description = "Submit, grade, and review homework submissions")
public class HomeworkSubmissionController {

    private final HomeworkSubmissionService submissionService;
    private final StudentRepository         studentRepository;

    public HomeworkSubmissionController(HomeworkSubmissionService submissionService,
                                        StudentRepository studentRepository) {
        this.submissionService = submissionService;
        this.studentRepository = studentRepository;
    }

    // ── Student: submit homework ──────────────────────────────────────────────

    @PostMapping("/{homeworkId}/submissions")
    @Operation(summary = "Submit homework — called by the logged-in student")
    @PreAuthorize("hasAnyRole('STUDENT','SCHOOL_ADMIN')")
    public ResponseEntity<HomeworkSubmissionDto> submit(
        @PathVariable UUID homeworkId,
        @Valid @RequestBody SubmitHomeworkRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        // Resolve the Student entity from the logged-in user's userId
        Student student = studentRepository
            .findByUserIdAndSchoolId(principal.userId(), principal.schoolId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Student profile not found for user " + principal.userId()));

        HomeworkSubmissionDto dto = submissionService.submit(
            principal.schoolId(), homeworkId, student.getId(), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    // ── Teacher: grade a submission ───────────────────────────────────────────

    @PostMapping("/{homeworkId}/submissions/{studentId}/grade")
    @Operation(summary = "Grade a student's submission")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<HomeworkSubmissionDto> grade(
        @PathVariable UUID homeworkId,
        @PathVariable UUID studentId,
        @Valid @RequestBody GradeSubmissionRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        HomeworkSubmissionDto dto = submissionService.grade(
            principal.schoolId(), homeworkId, studentId, principal.userId(), request);

        return ResponseEntity.ok(dto);
    }

    // ── Teacher / Admin: all submissions for a homework ───────────────────────

    @GetMapping("/{homeworkId}/submissions")
    @Operation(summary = "List all submissions for a homework assignment")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<List<HomeworkSubmissionDto>> getSubmissions(
        @PathVariable UUID homeworkId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(
            submissionService.getSubmissions(principal.schoolId(), homeworkId));
    }

    // ── Student: view own submission ──────────────────────────────────────────

    @GetMapping("/{homeworkId}/submissions/me")
    @Operation(summary = "Fetch the logged-in student's own submission for a homework")
    @PreAuthorize("hasAnyRole('STUDENT','PARENT','SCHOOL_ADMIN')")
    public ResponseEntity<HomeworkSubmissionDto> getMySubmission(
        @PathVariable UUID homeworkId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        Student student = studentRepository
            .findByUserIdAndSchoolId(principal.userId(), principal.schoolId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Student profile not found for user " + principal.userId()));

        return ResponseEntity.ok(
            submissionService.getMySubmission(principal.schoolId(), homeworkId, student.getId()));
    }

    // ── Teacher: submission counts by status ─────────────────────────────────

    @GetMapping("/{homeworkId}/submissions/summary")
    @Operation(summary = "Get submission status counts (PENDING / SUBMITTED / LATE / GRADED)")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<Map<String, Long>> getSummary(
        @PathVariable UUID homeworkId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(
            submissionService.getSummary(principal.schoolId(), homeworkId));
    }

    // ── Admin / Teacher / Parent: full submission history for a student ───────

    @GetMapping("/students/{studentId}/submissions")
    @Operation(summary = "Get all homework submissions for a specific student")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','TEACHER','PARENT')")
    public ResponseEntity<List<HomeworkSubmissionDto>> getStudentHistory(
        @PathVariable UUID studentId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(
            submissionService.getStudentHistory(principal.schoolId(), studentId));
    }
}
