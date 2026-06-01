package com.sms.api.controller;

import com.sms.api.dto.attendance.AttendanceSummaryDto;
import com.sms.api.dto.dashboard.StudentDashboardDto;
import com.sms.api.dto.homework.HomeworkDto;
import com.sms.api.dto.timetable.TimetableSlotDto;
import com.sms.api.entity.AcademicYear;
import com.sms.api.entity.Student;
import com.sms.api.repository.AcademicYearRepository;
import com.sms.api.repository.StudentRepository;
import com.sms.api.security.UserPrincipal;
import com.sms.api.service.AttendanceService;
import com.sms.api.service.HomeworkService;
import com.sms.api.service.TimetableService;
import com.sms.core.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/student")
@PreAuthorize("hasRole('STUDENT')")
@Tag(name = "Student Portal", description = "Student self-service dashboard")
public class StudentPortalController {

    private final StudentRepository     studentRepository;
    private final AcademicYearRepository yearRepository;
    private final AttendanceService     attendanceService;
    private final HomeworkService       homeworkService;
    private final TimetableService      timetableService;

    public StudentPortalController(StudentRepository studentRepository,
                                   AcademicYearRepository yearRepository,
                                   AttendanceService attendanceService,
                                   HomeworkService homeworkService,
                                   TimetableService timetableService) {
        this.studentRepository = studentRepository;
        this.yearRepository    = yearRepository;
        this.attendanceService = attendanceService;
        this.homeworkService   = homeworkService;
        this.timetableService  = timetableService;
    }

    @GetMapping("/me")
    public ResponseEntity<StudentDashboardDto> myDashboard(
            @AuthenticationPrincipal UserPrincipal principal) {

        UUID schoolId = principal.schoolId();
        UUID userId   = principal.userId();

        Student student = studentRepository.findByUserIdAndSchoolId(userId, schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("Student account", userId));

        UUID studentId = student.getId();
        UUID classId   = student.getSchoolClass() != null ? student.getSchoolClass().getId() : null;
        String className = student.getSchoolClass() != null ? student.getSchoolClass().getName() : null;

        // Attendance — current academic year start → today
        AcademicYear currentYear = yearRepository.findBySchoolIdAndIsCurrentTrue(schoolId).orElse(null);
        LocalDate from = currentYear != null ? currentYear.getStartDate() : LocalDate.now().withDayOfMonth(1);
        LocalDate to   = LocalDate.now();

        AttendanceSummaryDto attendance = attendanceService.getStudentSummary(schoolId, studentId, from, to);

        // Upcoming homework
        List<HomeworkDto> homework = classId != null
            ? homeworkService.getUpcoming(classId)
            : List.of();

        // Today's timetable — filter full class timetable by today's day of week
        List<TimetableSlotDto> todayTimetable = List.of();
        if (classId != null && currentYear != null) {
            String today = todayDayCode();
            todayTimetable = timetableService
                .getClassTimetable(schoolId, classId, currentYear.getId())
                .stream()
                .filter(s -> today.equals(s.dayOfWeek()))
                .toList();
        }

        return ResponseEntity.ok(new StudentDashboardDto(
            studentId,
            student.getFirstName() + " " + student.getLastName(),
            student.getAdmissionNo(),
            student.getRollNo(),
            classId,
            className,
            attendance,
            homework,
            todayTimetable
        ));
    }

    private String todayDayCode() {
        Map<DayOfWeek, String> map = Map.of(
            DayOfWeek.MONDAY,    "MON",
            DayOfWeek.TUESDAY,   "TUE",
            DayOfWeek.WEDNESDAY, "WED",
            DayOfWeek.THURSDAY,  "THU",
            DayOfWeek.FRIDAY,    "FRI",
            DayOfWeek.SATURDAY,  "SAT",
            DayOfWeek.SUNDAY,    "SUN"
        );
        return map.getOrDefault(LocalDate.now().getDayOfWeek(), "MON");
    }
}
