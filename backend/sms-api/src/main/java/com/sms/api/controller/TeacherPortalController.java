package com.sms.api.controller;

import com.sms.api.dto.dashboard.TeacherDashboardDto;
import com.sms.api.dto.homework.HomeworkDto;
import com.sms.api.dto.timetable.TimetableSlotDto;
import com.sms.api.entity.AcademicYear;
import com.sms.api.entity.User;
import com.sms.api.repository.AcademicYearRepository;
import com.sms.api.repository.UserRepository;
import com.sms.api.security.UserPrincipal;
import com.sms.api.service.HomeworkService;
import com.sms.api.service.TimetableService;
import com.sms.core.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/teacher")
@PreAuthorize("hasRole('TEACHER')")
@Tag(name = "Teacher Portal", description = "Teacher self-service dashboard")
public class TeacherPortalController {

    private final UserRepository        userRepository;
    private final AcademicYearRepository yearRepository;
    private final TimetableService      timetableService;
    private final HomeworkService       homeworkService;

    public TeacherPortalController(UserRepository userRepository,
                                    AcademicYearRepository yearRepository,
                                    TimetableService timetableService,
                                    HomeworkService homeworkService) {
        this.userRepository   = userRepository;
        this.yearRepository   = yearRepository;
        this.timetableService = timetableService;
        this.homeworkService  = homeworkService;
    }

    @GetMapping("/me")
    public ResponseEntity<TeacherDashboardDto> myDashboard(
            @AuthenticationPrincipal UserPrincipal principal) {

        UUID schoolId  = principal.schoolId();
        UUID teacherId = principal.userId();

        User teacher = userRepository.findById(teacherId)
            .orElseThrow(() -> new ResourceNotFoundException("Teacher", teacherId));

        AcademicYear currentYear = yearRepository.findBySchoolIdAndIsCurrentTrue(schoolId).orElse(null);

        // Today's timetable for this teacher
        List<TimetableSlotDto> todaySchedule = List.of();
        if (currentYear != null) {
            String today = todayDayCode();
            todaySchedule = timetableService
                .getTeacherTimetable(schoolId, teacherId, currentYear.getId())
                .stream()
                .filter(s -> today.equals(s.dayOfWeek()))
                .sorted((a, b) -> a.startTime().compareTo(b.startTime()))
                .toList();
        }

        // Recent homework created by this teacher (last 10)
        List<HomeworkDto> pending = homeworkService
            .list(schoolId, null, null, null, null, PageRequest.of(0, 10))
            .stream()
            .filter(h -> teacherId.toString().equals(
                h.teacherId() != null ? h.teacherId().toString() : ""))
            .toList();

        return ResponseEntity.ok(new TeacherDashboardDto(
            teacher.getFullName(),
            todaySchedule,
            pending,
            pending.size(),
            (long) todaySchedule.size()
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
