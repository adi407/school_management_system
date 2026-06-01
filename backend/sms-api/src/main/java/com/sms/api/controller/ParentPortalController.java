package com.sms.api.controller;

import com.sms.api.dto.announcement.AnnouncementDto;
import com.sms.api.dto.attendance.AttendanceSummaryDto;
import com.sms.api.dto.dashboard.ParentDashboardDto;
import com.sms.api.dto.fee.StudentFeesSummaryDto;
import com.sms.api.dto.homework.HomeworkDto;
import com.sms.api.entity.AcademicYear;
import com.sms.api.entity.Guardian;
import com.sms.api.entity.Student;
import com.sms.api.repository.AcademicYearRepository;
import com.sms.api.repository.GuardianRepository;
import com.sms.api.security.UserPrincipal;
import com.sms.api.service.AnnouncementService;
import com.sms.api.service.AttendanceService;
import com.sms.api.service.FeeService;
import com.sms.api.service.HomeworkService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/parent")
@PreAuthorize("hasRole('PARENT')")
@Tag(name = "Parent Portal", description = "Parent self-service dashboard")
public class ParentPortalController {

    private final GuardianRepository     guardianRepository;
    private final AcademicYearRepository yearRepository;
    private final AttendanceService      attendanceService;
    private final FeeService             feeService;
    private final HomeworkService        homeworkService;
    private final AnnouncementService    announcementService;

    public ParentPortalController(GuardianRepository guardianRepository,
                                   AcademicYearRepository yearRepository,
                                   AttendanceService attendanceService,
                                   FeeService feeService,
                                   HomeworkService homeworkService,
                                   AnnouncementService announcementService) {
        this.guardianRepository  = guardianRepository;
        this.yearRepository      = yearRepository;
        this.attendanceService   = attendanceService;
        this.feeService          = feeService;
        this.homeworkService     = homeworkService;
        this.announcementService = announcementService;
    }

    @GetMapping("/my-child")
    public ResponseEntity<ParentDashboardDto> myChild(
            @AuthenticationPrincipal UserPrincipal principal) {

        UUID   schoolId    = principal.schoolId();
        String parentEmail = principal.email();

        List<Guardian> guardians = guardianRepository
            .findByEmailAndSchoolIdOrderByIsPrimaryDesc(parentEmail, schoolId);

        if (guardians.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                "No child linked to parent account: " + parentEmail);
        }

        // Primary child first
        Guardian guardian = guardians.get(0);
        Student  student  = guardian.getStudent();
        UUID     studentId = student.getId();
        UUID     classId   = student.getSchoolClass() != null ? student.getSchoolClass().getId() : null;
        String   className = student.getSchoolClass() != null ? student.getSchoolClass().getName() : null;

        // Date range — current academic year start → today
        AcademicYear currentYear = yearRepository.findBySchoolIdAndIsCurrentTrue(schoolId).orElse(null);
        LocalDate from = currentYear != null ? currentYear.getStartDate() : LocalDate.now().withDayOfMonth(1);
        LocalDate to   = LocalDate.now();

        AttendanceSummaryDto attendance = attendanceService.getStudentSummary(schoolId, studentId, from, to);

        StudentFeesSummaryDto fees = feeService.getStudentFeesSummary(schoolId, studentId);

        List<HomeworkDto> homework = classId != null
            ? homeworkService.getUpcoming(classId)
            : List.of();

        List<AnnouncementDto> announcements = announcementService.listActive(schoolId);

        return ResponseEntity.ok(new ParentDashboardDto(
            studentId,
            student.getFirstName() + " " + student.getLastName(),
            student.getAdmissionNo(),
            student.getRollNo(),
            classId,
            className,
            guardian.getName(),
            guardian.getRelation(),
            attendance,
            fees,
            homework,
            announcements
        ));
    }
}
