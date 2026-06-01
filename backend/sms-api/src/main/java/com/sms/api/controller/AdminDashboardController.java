package com.sms.api.controller;

import com.sms.api.dto.dashboard.AdminDashboardDto;
import com.sms.api.entity.FeePayment;
import com.sms.api.entity.Student;
import com.sms.api.repository.*;
import com.sms.api.security.UserPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dashboard")
@PreAuthorize("hasAnyRole('SCHOOL_ADMIN','ACCOUNTANT','LIBRARIAN')")
@Tag(name = "Admin Dashboard", description = "Live KPI stats for the admin dashboard")
public class AdminDashboardController {

    private final StudentRepository     studentRepository;
    private final AttendanceRepository  attendanceRepository;
    private final FeePaymentRepository  feePaymentRepository;
    private final FeeStructureRepository feeStructureRepository;
    private final UserRepository        userRepository;
    private final ExamRepository        examRepository;
    private final BookIssueRepository   bookIssueRepository;

    public AdminDashboardController(StudentRepository studentRepository,
                                     AttendanceRepository attendanceRepository,
                                     FeePaymentRepository feePaymentRepository,
                                     FeeStructureRepository feeStructureRepository,
                                     UserRepository userRepository,
                                     ExamRepository examRepository,
                                     BookIssueRepository bookIssueRepository) {
        this.studentRepository      = studentRepository;
        this.attendanceRepository   = attendanceRepository;
        this.feePaymentRepository   = feePaymentRepository;
        this.feeStructureRepository = feeStructureRepository;
        this.userRepository         = userRepository;
        this.examRepository         = examRepository;
        this.bookIssueRepository    = bookIssueRepository;
    }

    @GetMapping("/admin")
    public ResponseEntity<AdminDashboardDto> stats(@AuthenticationPrincipal UserPrincipal p) {
        UUID schoolId = p.schoolId();
        LocalDate today = LocalDate.now();

        // ── Students ─────────────────────────────────────────────────────────
        long totalStudents  = studentRepository.countBySchoolId(schoolId);
        long activeStudents = studentRepository.countBySchoolIdAndIsActive(schoolId, true);

        // ── Today's attendance % ─────────────────────────────────────────────
        long totalMarked   = attendanceRepository.countBySchoolIdAndAttendanceDate(schoolId, today);
        long presentToday  = attendanceRepository.countPresentOnDate(schoolId, today);
        double attendancePct = totalMarked > 0
            ? Math.round((presentToday * 100.0 / totalMarked) * 10) / 10.0
            : 0.0;

        // ── Fees ─────────────────────────────────────────────────────────────
        BigDecimal collectedToday  = feePaymentRepository.sumCollectedOnDate(schoolId, today);
        BigDecimal totalCollected  = feePaymentRepository.sumTotalCollected(schoolId);

        // Total fees due = sum of all fee structures for this school
        // Pending = structures total - collected
        BigDecimal totalDue = feeStructureRepository.sumTotalFeesBySchool(schoolId);
        BigDecimal pending  = totalDue.subtract(totalCollected).max(BigDecimal.ZERO);

        // ── Staff ────────────────────────────────────────────────────────────
        long staffCount = userRepository.countStaffBySchoolId(schoolId);

        // ── Exams ────────────────────────────────────────────────────────────
        long upcomingExams = examRepository.countBySchoolIdAndStatusAndStartDateAfter(
            schoolId, "UPCOMING", today.minusDays(1));

        // ── Library ──────────────────────────────────────────────────────────
        long overdueBooks = bookIssueRepository.countBySchoolIdAndIsReturnedFalseAndDueDateBefore(
            schoolId, today);

        // ── Recent Activity (last 5 fee payments) ────────────────────────────
        List<AdminDashboardDto.ActivityItem> activity = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d MMM");
        feePaymentRepository.findTop5BySchoolIdOrderByPaymentDateDescIdDesc(schoolId)
            .forEach(fp -> {
                String studentName = fp.getStudent() != null
                    ? fp.getStudent().getFirstName() + " " + fp.getStudent().getLastName()
                    : "Unknown";
                activity.add(new AdminDashboardDto.ActivityItem(
                    "Fee collected from " + studentName + " — ₹" + fp.getAmountPaid().toPlainString(),
                    "fee",
                    fp.getPaymentDate().format(fmt)
                ));
            });

        // Pad with static context items if fewer than 5 payments
        if (activeStudents > 0 && activity.size() < 5) {
            activity.add(new AdminDashboardDto.ActivityItem(
                activeStudents + " students active this academic year", "student", "This year"
            ));
        }
        if (totalMarked > 0 && activity.size() < 5) {
            activity.add(new AdminDashboardDto.ActivityItem(
                "Attendance recorded: " + presentToday + "/" + totalMarked + " present today",
                "attendance", "Today"
            ));
        }

        return ResponseEntity.ok(new AdminDashboardDto(
            totalStudents, activeStudents,
            attendancePct,
            collectedToday, pending,
            staffCount, upcomingExams, overdueBooks,
            activity
        ));
    }
}
