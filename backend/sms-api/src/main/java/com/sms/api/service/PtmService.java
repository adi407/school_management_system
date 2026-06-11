package com.sms.api.service;

import com.sms.api.dto.ptm.*;
import com.sms.api.entity.*;
import com.sms.api.repository.*;
import com.sms.core.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

/**
 * PTM Prep AI — generates per-student briefings for Parent-Teacher Meetings.
 *
 * For each student in a class, aggregates:
 * - Attendance % (from attendance table)
 * - Average exam marks (from exam results — placeholder)
 * - Homework completion % (from homework_submissions)
 * - Wellness trend (from wellness_checkins)
 *
 * Then generates a narrative summary, talking points, and parent-friendly preview.
 * Initially uses template-based generation; plug in Gemini/Claude API later.
 */
@Service
@Transactional
public class PtmService {

    private static final Logger log = LoggerFactory.getLogger(PtmService.class);

    private final PtmMeetingRepository meetingRepo;
    private final PtmBriefingRepository briefingRepo;
    private final StudentRepository studentRepo;
    private final AttendanceRepository attendanceRepo;
    private final HomeworkSubmissionRepository hwSubRepo;
    private final HomeworkRepository hwRepo;
    private final WellnessCheckinRepository wellnessRepo;
    private final SchoolClassRepository classRepo;
    private final AcademicYearRepository yearRepo;
    private final UserRepository userRepo;

    public PtmService(PtmMeetingRepository meetingRepo,
                       PtmBriefingRepository briefingRepo,
                       StudentRepository studentRepo,
                       AttendanceRepository attendanceRepo,
                       HomeworkSubmissionRepository hwSubRepo,
                       HomeworkRepository hwRepo,
                       WellnessCheckinRepository wellnessRepo,
                       SchoolClassRepository classRepo,
                       AcademicYearRepository yearRepo,
                       UserRepository userRepo) {
        this.meetingRepo    = meetingRepo;
        this.briefingRepo   = briefingRepo;
        this.studentRepo    = studentRepo;
        this.attendanceRepo = attendanceRepo;
        this.hwSubRepo      = hwSubRepo;
        this.hwRepo         = hwRepo;
        this.wellnessRepo   = wellnessRepo;
        this.classRepo      = classRepo;
        this.yearRepo       = yearRepo;
        this.userRepo       = userRepo;
    }

    // ── Meeting CRUD ─────────────────────────────────────────

    public PtmMeetingDto createMeeting(UUID schoolId, CreatePtmRequest req, UUID createdById) {
        User creator = userRepo.findById(createdById)
            .orElseThrow(() -> new ResourceNotFoundException("User", createdById));

        AcademicYear ay = yearRepo.findByIdAndSchoolId(req.academicYearId(), schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", req.academicYearId()));

        PtmMeeting meeting = new PtmMeeting();
        meeting.setSchoolId(schoolId);
        meeting.setAcademicYear(ay);
        meeting.setTitle(req.title());
        meeting.setMeetingDate(req.meetingDate());
        meeting.setStartTime(req.startTime());
        meeting.setEndTime(req.endTime());
        meeting.setNotes(req.notes());
        meeting.setCreatedBy(creator);

        if (req.classId() != null) {
            SchoolClass sc = classRepo.findByIdAndSchoolId(req.classId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Class", req.classId()));
            meeting.setSchoolClass(sc);
        }

        return toMeetingDto(meetingRepo.save(meeting));
    }

    @Transactional(readOnly = true)
    public List<PtmMeetingDto> listMeetings(UUID schoolId) {
        return meetingRepo.findBySchoolIdOrderByMeetingDateDesc(schoolId)
            .stream().map(this::toMeetingDto).toList();
    }

    @Transactional(readOnly = true)
    public PtmMeetingDto getMeeting(UUID schoolId, UUID meetingId) {
        PtmMeeting m = meetingRepo.findByIdAndSchoolId(meetingId, schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("PtmMeeting", meetingId));
        return toMeetingDto(m);
    }

    // ── AI Briefing Generation ───────────────────────────────

    /**
     * Generate briefings for all students in the PTM meeting's class.
     * Aggregates data from attendance, homework, wellness tables.
     */
    public List<PtmBriefingDto> generateBriefings(UUID schoolId, UUID meetingId) {
        PtmMeeting meeting = meetingRepo.findByIdAndSchoolId(meetingId, schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("PtmMeeting", meetingId));

        if (meeting.getSchoolClass() == null) {
            throw new IllegalStateException("Cannot generate briefings for school-wide PTM — select a class first");
        }

        UUID classId = meeting.getSchoolClass().getId();
        List<Student> students = studentRepo.findBySchoolClassIdAndIsActiveTrueOrderByFirstNameAsc(classId);

        // Date range for aggregation: start of academic year to meeting date
        AcademicYear ay = meeting.getAcademicYear();
        LocalDate fromDate = ay.getStartDate();
        LocalDate toDate = meeting.getMeetingDate();

        List<PtmBriefing> briefings = new ArrayList<>();

        for (Student student : students) {
            // Skip if briefing already exists
            Optional<PtmBriefing> existing = briefingRepo.findByPtmMeetingIdAndStudentId(meetingId, student.getId());
            if (existing.isPresent()) {
                briefings.add(existing.get());
                continue;
            }

            // ── Aggregate attendance ──────────────────────────
            long totalDays = attendanceRepo.countByStudentIdAndAttendanceDateBetween(
                student.getId(), fromDate, toDate);
            long presentDays = attendanceRepo.countByStudentIdAndStatusAndAttendanceDateBetween(
                student.getId(), "PRESENT", fromDate, toDate);
            long lateDays = attendanceRepo.countByStudentIdAndStatusAndAttendanceDateBetween(
                student.getId(), "LATE", fromDate, toDate);
            float attPct = totalDays > 0 ? ((presentDays + lateDays) * 100f / totalDays) : 0;

            // ── Homework completion (placeholder — count submissions vs assignments)
            float hwPct = 0; // Will be computed when homework data is populated

            // ── Wellness trend ────────────────────────────────
            String wellnessTrend = computeWellnessTrend(student.getId(), classId, fromDate, toDate);

            // ── Average marks (placeholder) ───────────────────
            Float avgMarks = null; // Will be computed when exam results are populated

            // ── Generate AI narrative ─────────────────────────
            String studentName = student.getFirstName();
            String summary = generateSummary(studentName, attPct, avgMarks, hwPct, wellnessTrend);
            String talkingPoints = generateTalkingPoints(studentName, attPct, avgMarks, hwPct, wellnessTrend);
            String parentPreview = generateParentPreview(studentName, attPct, avgMarks, hwPct, wellnessTrend);

            PtmBriefing briefing = new PtmBriefing();
            briefing.setPtmMeeting(meeting);
            briefing.setStudent(student);
            briefing.setAttendancePct(attPct);
            briefing.setAvgMarks(avgMarks);
            briefing.setHomeworkCompletionPct(hwPct);
            briefing.setWellnessTrend(wellnessTrend);
            briefing.setAiSummary(summary);
            briefing.setTalkingPoints(talkingPoints);
            briefing.setParentPreview(parentPreview);

            briefings.add(briefingRepo.save(briefing));
        }

        log.info("Generated {} briefings for PTM meeting {}", briefings.size(), meetingId);
        return briefings.stream().map(this::toBriefingDto).toList();
    }

    @Transactional(readOnly = true)
    public List<PtmBriefingDto> getBriefings(UUID schoolId, UUID meetingId) {
        // Verify meeting belongs to school
        meetingRepo.findByIdAndSchoolId(meetingId, schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("PtmMeeting", meetingId));

        return briefingRepo.findByPtmMeetingIdOrderByStudentId(meetingId)
            .stream().map(this::toBriefingDto).toList();
    }

    /** Mark briefing as reviewed by teacher */
    public PtmBriefingDto reviewBriefing(UUID briefingId, UUID reviewerId) {
        PtmBriefing b = briefingRepo.findById(briefingId)
            .orElseThrow(() -> new ResourceNotFoundException("PtmBriefing", briefingId));
        User reviewer = userRepo.findById(reviewerId).orElse(null);
        b.setStatus("REVIEWED");
        b.setReviewedBy(reviewer);
        return toBriefingDto(briefingRepo.save(b));
    }

    // ── AI Generation (template-based — swap to Gemini/Claude later) ──

    /**
     * Template-based summary generation.
     * TODO: Replace with actual AI API call (Gemini Flash / Claude Haiku)
     */
    private String generateSummary(String name, float attPct, Float avgMarks, float hwPct, String wellnessTrend) {
        StringBuilder sb = new StringBuilder();

        // Attendance insight
        if (attPct >= 90) {
            sb.append(name).append(" maintains excellent attendance at ").append(String.format("%.0f", attPct)).append("%. ");
        } else if (attPct >= 75) {
            sb.append(name).append("'s attendance is ").append(String.format("%.0f", attPct)).append("% — acceptable but could improve. ");
        } else if (attPct > 0) {
            sb.append(name).append("'s attendance is concerning at ").append(String.format("%.0f", attPct)).append("%. This needs immediate attention. ");
        } else {
            sb.append("No attendance data available for ").append(name).append(" yet. ");
        }

        // Academic insight
        if (avgMarks != null) {
            if (avgMarks >= 80) sb.append("Academically strong with ").append(String.format("%.0f", avgMarks)).append("% average. ");
            else if (avgMarks >= 60) sb.append("Academic performance is average at ").append(String.format("%.0f", avgMarks)).append("%. ");
            else sb.append("Academic performance needs support — ").append(String.format("%.0f", avgMarks)).append("% average. ");
        }

        // Wellness
        if ("DECLINING".equals(wellnessTrend)) {
            sb.append("Wellness check-ins show a declining trend — counselor follow-up recommended.");
        } else if ("IMPROVING".equals(wellnessTrend)) {
            sb.append("Wellness indicators are improving — positive trajectory.");
        }

        return sb.toString().trim();
    }

    private String generateTalkingPoints(String name, float attPct, Float avgMarks, float hwPct, String wellnessTrend) {
        List<String> points = new ArrayList<>();

        if (attPct < 75 && attPct > 0) {
            points.add("Attendance is below 75% — discuss possible reasons and improvement plan");
        }
        if (attPct >= 90) {
            points.add("Appreciate consistent attendance — acknowledge " + name + "'s discipline");
        }
        if (avgMarks != null && avgMarks < 60) {
            points.add("Academic support needed — suggest extra coaching or peer tutoring");
        }
        if (avgMarks != null && avgMarks >= 85) {
            points.add("Excellent academic performance — discuss advanced opportunities or competitions");
        }
        if ("DECLINING".equals(wellnessTrend)) {
            points.add("Wellness declining — sensitively explore if there are challenges at home or with peers");
        }
        if (hwPct > 0 && hwPct < 50) {
            points.add("Homework completion is low — discuss time management and support at home");
        }

        if (points.isEmpty()) {
            points.add("General check-in — " + name + " is doing well overall");
            points.add("Ask parents about any concerns or areas they'd like to discuss");
        }

        return String.join("\n", points.stream().map(p -> "• " + p).toList());
    }

    private String generateParentPreview(String name, float attPct, Float avgMarks, float hwPct, String wellnessTrend) {
        StringBuilder sb = new StringBuilder();
        sb.append("Here's a quick snapshot of ").append(name).append("'s progress:\n\n");

        if (attPct > 0) sb.append("Attendance: ").append(String.format("%.0f", attPct)).append("%\n");
        if (avgMarks != null) sb.append("Average Score: ").append(String.format("%.0f", avgMarks)).append("%\n");

        if (attPct >= 75 && (avgMarks == null || avgMarks >= 60)) {
            sb.append("\n").append(name).append(" is progressing well. We look forward to discussing their journey with you.");
        } else {
            sb.append("\nWe'd like to discuss some areas where we can work together to support ").append(name).append("'s growth.");
        }

        return sb.toString();
    }

    private String computeWellnessTrend(UUID studentId, UUID classId, LocalDate from, LocalDate to) {
        // Simple heuristic: compare last 2 weeks to previous 2 weeks
        // Since wellness is anonymous (student_id can be null), we use class-level data
        // This is a simplified version — will be enhanced with AI later
        return "STABLE";
    }

    // ── DTO Mappers ──────────────────────────────────────────

    private PtmMeetingDto toMeetingDto(PtmMeeting m) {
        SchoolClass sc = m.getSchoolClass();
        User creator = m.getCreatedBy();
        long briefingCount = briefingRepo.countByPtmMeetingId(m.getId());

        return new PtmMeetingDto(
            m.getId(),
            m.getTitle(),
            sc != null ? sc.getId() : null,
            sc != null ? sc.getName() : null,
            m.getAcademicYear().getId(),
            m.getMeetingDate(),
            m.getStartTime(),
            m.getEndTime(),
            m.getStatus(),
            m.getNotes(),
            creator != null ? creator.getFullName() : null,
            (int) briefingCount
        );
    }

    private PtmBriefingDto toBriefingDto(PtmBriefing b) {
        Student s = b.getStudent();
        User t = b.getTeacher();

        return new PtmBriefingDto(
            b.getId(),
            b.getPtmMeeting().getId(),
            s != null ? s.getId() : null,
            s != null ? (s.getFirstName() + " " + s.getLastName()) : null,
            s != null && s.getSchoolClass() != null ? s.getSchoolClass().getName() : null,
            t != null ? t.getId() : null,
            t != null ? t.getFullName() : null,
            b.getAttendancePct(),
            b.getAvgMarks(),
            b.getHomeworkCompletionPct(),
            b.getWellnessTrend(),
            b.getAiSummary(),
            b.getTalkingPoints(),
            b.getParentPreview(),
            b.getStatus()
        );
    }
}
