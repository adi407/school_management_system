package com.sms.api.controller;

import com.sms.api.dto.exam.CreateExamRequest;
import com.sms.api.dto.exam.ExamDto;
import com.sms.api.security.UserPrincipal;
import com.sms.api.service.ExamService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/exams")
@PreAuthorize("isAuthenticated()")
@Tag(name = "Exams", description = "Exam schedule management")
public class ExamController {

    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    @GetMapping
    public ResponseEntity<List<ExamDto>> list(@AuthenticationPrincipal UserPrincipal p) {
        return ResponseEntity.ok(examService.list(p.schoolId()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN')")
    public ResponseEntity<ExamDto> create(@Valid @RequestBody CreateExamRequest req,
                                          @AuthenticationPrincipal UserPrincipal p) {
        ExamDto dto = examService.create(p.schoolId(), req);
        return ResponseEntity.created(URI.create("/api/v1/exams/" + dto.id())).body(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN')")
    public ResponseEntity<ExamDto> update(@PathVariable UUID id,
                                          @Valid @RequestBody CreateExamRequest req,
                                          @AuthenticationPrincipal UserPrincipal p) {
        return ResponseEntity.ok(examService.update(p.schoolId(), id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                       @AuthenticationPrincipal UserPrincipal p) {
        examService.delete(p.schoolId(), id);
        return ResponseEntity.noContent().build();
    }
}
