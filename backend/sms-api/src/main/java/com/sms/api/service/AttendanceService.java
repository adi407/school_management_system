package com.sms.api.service;

import com.sms.api.dto.attendance.AttendanceEntryRequest;
import com.sms.api.dto.attendance.AttendanceRecordDto;
import com.sms.api.dto.attendance.AttendanceSummaryDto;
import com.sms.api.dto.attendance.MarkAttendanceRequest;
import com.sms.api.entity.*;
import com.sms.api.repository.AttendanceRepository;
import com.sms.api.repository.GuardianRepository;
import com.sms.api.repository.SchoolClassRepository;
import com.sms.api.repository.StudentRepository;
import com.sms.api.repository.UserRepository;
import com.sms.core.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class AttendanceService {

    private static final Set<String> ALERT_STATUSES = Set.of("ABSENT", "LATE", "EXCUSED");

    private final AttendanceRepository  attendanceRepository;
    private final SchoolClassRepository classRepository;
    private final StudentRepository     studentRepository;
    private final UserRepository        userRepository;
    private final GuardianRepository    guardianRepository;
    private final EmailService          emailService;

    public AttendanceService(AttendanceRepository attendanceRepository,
                             SchoolClassRepository classRepository,
                             StudentRepository studentRepository,
                             UserRepository userRepository,
                             GuardianRepository guardianRepository,
                             EmailService emailService) {
        this.attendanceRepository = attendanceRepository;
        this.classRepository      = classRepository;
        this.studentRepository    = studentRepository;
        this.userRepository       = userRepository;
        this.guardianRepository   = guardianRepository;
        this.emailService         = emailService;
    }

    // ── Teacher/Admin: bulk mark attendance for a class on a date ────────────

    public List<AttendanceRecordDto> markAttendance(UUID schoolId, UUID markedByUserId,
                                                     MarkAttendanceRequest req) {
        SchoolClass schoolClass = classRepository.findByIdAndSchoolId(req.classId(), schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("Class", req.classId()));

        User marker = userRepository.findById(markedByUserId)
            .orElseThrow(() -> new ResourceNotFoundException("User", markedByUserId));

        // Load existing records for this class+date to allow updates (idempotent)
        Map<UUID, Attendance> existing = attendanceRepository
            .findBySchoolClassIdAndAttendanceDateOrderByStudentId(req.classId(), req.date())
            .stream()
            .collect(Collectors.toMap(a -> a.getStudent().getId(), Function.identity()));

        List<Attendance> toSave = req.entries().stream().map(entry -> {
            Student student = studentRepository.findByIdAndSchoolId(entry.studentId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", entry.studentId()));

            Attendance att = existing.getOrDefault(entry.studentId(), new Attendance());
            att.setSchoolId(schoolId);
            att.setStudent(student);
            att.setSchoolClass(schoolClass);
            att.setMarkedBy(marker);
            att.setAttendanceDate(req.date());
            att.setStatus(entry.status());
            att.setRemarks(entry.remarks());
            return att;
        }).collect(Collectors.toList());

        List<Attendance> saved = attendanceRepository.saveAll(toSave);

        saved.stream()
            .filter(a -> ALERT_STATUSES.contains(a.getStatus()))
            .forEach(a -> {
                List<Guardian> guardians = guardianRepository
                    .findByStudentIdOrderByIsPrimaryDesc(a.getStudent().getId());
                if (!guardians.isEmpty()) {
                    emailService.sendAttendanceAlert(guardians, a.getStudent(),
                        a.getAttendanceDate(), a.getStatus());
                }
            });

        return saved.stream().map(this::toDto).collect(Collectors.toList());
    }

    // ── Get roll call for a class on a date ──────────────────────────────────

    @Transactional(readOnly = true)
    public List<AttendanceRecordDto> getClassRoll(UUID schoolId, UUID classId, LocalDate date) {
        classRepository.findByIdAndSchoolId(classId, schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("Class", classId));

        return attendanceRepository
            .findBySchoolClassIdAndAttendanceDateOrderByStudentId(classId, date)
            .stream().map(this::toDto).toList();
    }

    // ── Student's own history in a date range ────────────────────────────────

    @Transactional(readOnly = true)
    public List<AttendanceRecordDto> getStudentHistory(UUID schoolId, UUID studentId,
                                                        LocalDate from, LocalDate to) {
        studentRepository.findByIdAndSchoolId(studentId, schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));

        return attendanceRepository
            .findByStudentIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(studentId, from, to)
            .stream().map(this::toDto).toList();
    }

    // ── Attendance summary / percentage for a student ────────────────────────

    @Transactional(readOnly = true)
    public AttendanceSummaryDto getStudentSummary(UUID schoolId, UUID studentId,
                                                   LocalDate from, LocalDate to) {
        Student student = studentRepository.findByIdAndSchoolId(studentId, schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));

        long total   = attendanceRepository.countByStudentIdAndAttendanceDateBetween(studentId, from, to);
        long present = attendanceRepository.countByStudentIdAndStatusAndAttendanceDateBetween(studentId, "PRESENT", from, to);
        long absent  = attendanceRepository.countByStudentIdAndStatusAndAttendanceDateBetween(studentId, "ABSENT",  from, to);
        long late    = attendanceRepository.countByStudentIdAndStatusAndAttendanceDateBetween(studentId, "LATE",    from, to);

        double pct = total > 0 ? Math.round((present + late) * 1000.0 / total) / 10.0 : 0.0;

        return new AttendanceSummaryDto(
            studentId,
            student.getFirstName() + " " + student.getLastName(),
            total, present, absent, late, pct
        );
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private AttendanceRecordDto toDto(Attendance a) {
        Student     s  = a.getStudent();
        SchoolClass sc = a.getSchoolClass();
        User        mb = a.getMarkedBy();

        return new AttendanceRecordDto(
            a.getId(),
            s  != null ? s.getId()              : null,
            s  != null ? s.getFirstName() + " " + s.getLastName() : null,
            s  != null ? s.getAdmissionNo()     : null,
            sc != null ? sc.getId()             : null,
            sc != null ? sc.getName()           : null,
            a.getAttendanceDate(),
            a.getStatus(),
            a.getRemarks(),
            mb != null ? mb.getId()             : null,
            mb != null ? mb.getEmail()          : null
        );
    }
}
