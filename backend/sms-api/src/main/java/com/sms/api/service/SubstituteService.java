package com.sms.api.service;

import com.sms.api.dto.substitute.*;
import com.sms.api.entity.*;
import com.sms.api.repository.*;
import com.sms.core.enums.Role;
import com.sms.core.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Smart Substitute Teacher — AI-powered teacher replacement engine.
 *
 * When a teacher is marked absent, this service:
 * 1. Finds all their timetable slots for that day
 * 2. Creates SubstituteAssignment records (PENDING)
 * 3. For each slot, scores all available teachers and suggests the best match
 *
 * Scoring algorithm:
 * - Base: teacher must be FREE at that period (no timetable slot)
 * - +40 pts: teaches the same subject in another class
 * - +20 pts: same department
 * - -10 pts per existing substitute duty today (load balancing)
 * - -5 pts per period they're already teaching today (fatigue)
 */
@Service
@Transactional
public class SubstituteService {

    private static final Logger log = LoggerFactory.getLogger(SubstituteService.class);

    private final SubstituteAssignmentRepository assignmentRepo;
    private final TimetableSlotRepository timetableRepo;
    private final UserRepository userRepo;
    private final ClassSubjectTeacherRepository cstRepo;
    private final StaffAttendanceRepository staffAttRepo;

    public SubstituteService(SubstituteAssignmentRepository assignmentRepo,
                              TimetableSlotRepository timetableRepo,
                              UserRepository userRepo,
                              ClassSubjectTeacherRepository cstRepo,
                              StaffAttendanceRepository staffAttRepo) {
        this.assignmentRepo = assignmentRepo;
        this.timetableRepo  = timetableRepo;
        this.userRepo       = userRepo;
        this.cstRepo        = cstRepo;
        this.staffAttRepo   = staffAttRepo;
    }

    /**
     * Report a teacher absence — creates substitute assignments for all affected periods
     * and runs the suggestion algorithm.
     */
    public List<SubstituteAssignmentDto> reportAbsence(UUID schoolId, ReportAbsenceRequest req, UUID reportedBy) {
        User absentTeacher = userRepo.findByIdAndSchoolId(req.teacherId(), schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("Teacher", req.teacherId()));

        String dayCode = toDayCode(req.absenceDate());

        // 1. Find all timetable slots for this teacher on that day
        List<TimetableSlot> slots = timetableRepo.findByTeacherIdAndDayOfWeekOrderByPeriodNoAsc(
            req.teacherId(), dayCode);

        if (slots.isEmpty()) {
            return List.of(); // Teacher has no classes that day
        }

        // 2. Get all school slots for that day (to know who's busy when)
        List<TimetableSlot> allSlots = timetableRepo.findAllBySchoolAndDay(schoolId, dayCode);

        // 3. Get all active teachers in the school
        List<User> allTeachers = userRepo.findAllBySchoolIdAndRole(schoolId, Role.TEACHER)
            .stream().filter(User::isActive)
            .filter(t -> !t.getId().equals(req.teacherId()))
            .toList();

        // 4. Get subject-teacher mappings for scoring
        // Build: teacherId → set of subjectIds they teach
        Map<UUID, Set<UUID>> teacherSubjects = new HashMap<>();
        // We'll derive this from timetable slots
        for (TimetableSlot ts : allSlots) {
            if (ts.getTeacher() != null && ts.getSubject() != null) {
                teacherSubjects
                    .computeIfAbsent(ts.getTeacher().getId(), k -> new HashSet<>())
                    .add(ts.getSubject().getId());
            }
        }

        // 5. Build busy map: period → set of busy teacher IDs
        Map<Short, Set<UUID>> busyByPeriod = new HashMap<>();
        for (TimetableSlot ts : allSlots) {
            if (ts.getTeacher() != null) {
                busyByPeriod
                    .computeIfAbsent(ts.getPeriodNo(), k -> new HashSet<>())
                    .add(ts.getTeacher().getId());
            }
        }

        // 6. Count today's load per teacher
        Map<UUID, Integer> todayLoad = new HashMap<>();
        for (TimetableSlot ts : allSlots) {
            if (ts.getTeacher() != null) {
                todayLoad.merge(ts.getTeacher().getId(), 1, Integer::sum);
            }
        }

        // 7. Count existing substitute duties today
        Map<UUID, Long> existingSubDuties = new HashMap<>();
        for (User t : allTeachers) {
            long count = assignmentRepo.countBySubstituteTeacherIdAndAbsenceDate(t.getId(), req.absenceDate());
            if (count > 0) existingSubDuties.put(t.getId(), count);
        }

        // 8. Create assignments and generate suggestions for each slot
        List<SubstituteAssignment> assignments = new ArrayList<>();

        for (TimetableSlot slot : slots) {
            // Check if assignment already exists
            Optional<SubstituteAssignment> existing = assignmentRepo
                .findBySchoolClassIdAndPeriodNoAndAbsenceDate(
                    slot.getSchoolClass().getId(), slot.getPeriodNo(), req.absenceDate());
            if (existing.isPresent()) continue;

            // Score all free teachers for this period
            Set<UUID> busyTeachers = busyByPeriod.getOrDefault(slot.getPeriodNo(), Set.of());
            UUID targetSubjectId = slot.getSubject() != null ? slot.getSubject().getId() : null;

            SubstituteSuggestionDto bestSuggestion = null;
            float bestScore = -999;

            for (User candidate : allTeachers) {
                if (busyTeachers.contains(candidate.getId())) continue; // occupied

                float score = 0;
                boolean sameSubject = false;
                List<String> reasons = new ArrayList<>();

                // Free period = base qualification
                reasons.add("Free period " + slot.getPeriodNo());

                // Same subject bonus
                if (targetSubjectId != null) {
                    Set<UUID> candidateSubjects = teacherSubjects.getOrDefault(candidate.getId(), Set.of());
                    if (candidateSubjects.contains(targetSubjectId)) {
                        score += 40;
                        sameSubject = true;
                        reasons.add("teaches " + (slot.getSubject() != null ? slot.getSubject().getName() : "same subject"));
                    }
                }

                // Same department bonus
                if (absentTeacher.getDepartment() != null && absentTeacher.getDepartment().equals(candidate.getDepartment())) {
                    score += 20;
                    reasons.add("same department");
                }

                // Load balancing penalty
                long subDuties = existingSubDuties.getOrDefault(candidate.getId(), 0L);
                score -= subDuties * 10;

                int load = todayLoad.getOrDefault(candidate.getId(), 0);
                score -= load * 5;

                if (score > bestScore) {
                    bestScore = score;
                    bestSuggestion = new SubstituteSuggestionDto(
                        candidate.getId(),
                        candidate.getFullName(),
                        candidate.getDepartment(),
                        String.join(" + ", reasons),
                        Math.max(0, Math.min(1, (score + 50) / 100f)), // normalize to 0-1
                        sameSubject,
                        load
                    );
                }
            }

            // Create assignment
            SubstituteAssignment sa = new SubstituteAssignment();
            sa.setSchoolId(schoolId);
            sa.setAbsentTeacher(absentTeacher);
            sa.setAbsenceDate(req.absenceDate());
            sa.setPeriodNo(slot.getPeriodNo());
            sa.setSchoolClass(slot.getSchoolClass());
            sa.setSubject(slot.getSubject());
            sa.setStartTime(slot.getStartTime());
            sa.setEndTime(slot.getEndTime());
            sa.setRemarks(req.remarks());

            if (bestSuggestion != null) {
                User suggested = userRepo.findById(bestSuggestion.teacherId()).orElse(null);
                sa.setSubstituteTeacher(suggested);
                sa.setStatus("SUGGESTED");
                sa.setSuggestionReason(bestSuggestion.reason());
                sa.setConfidenceScore(bestSuggestion.confidenceScore());
            } else {
                sa.setStatus("PENDING");
                sa.setSuggestionReason("No available teacher found — consider self-study");
            }

            assignments.add(assignmentRepo.save(sa));
        }

        return assignments.stream().map(this::toDto).toList();
    }

    /**
     * Get suggestions for a specific assignment (re-run scoring).
     */
    @Transactional(readOnly = true)
    public List<SubstituteSuggestionDto> getSuggestions(UUID schoolId, UUID assignmentId) {
        SubstituteAssignment sa = assignmentRepo.findById(assignmentId)
            .filter(a -> schoolId.equals(a.getSchoolId()))
            .orElseThrow(() -> new ResourceNotFoundException("SubstituteAssignment", assignmentId));

        String dayCode = toDayCode(sa.getAbsenceDate());
        List<TimetableSlot> allSlots = timetableRepo.findAllBySchoolAndDay(schoolId, dayCode);

        Set<UUID> busyAtPeriod = allSlots.stream()
            .filter(ts -> ts.getPeriodNo() == sa.getPeriodNo() && ts.getTeacher() != null)
            .map(ts -> ts.getTeacher().getId())
            .collect(Collectors.toSet());

        Map<UUID, Set<UUID>> teacherSubjects = new HashMap<>();
        for (TimetableSlot ts : allSlots) {
            if (ts.getTeacher() != null && ts.getSubject() != null) {
                teacherSubjects.computeIfAbsent(ts.getTeacher().getId(), k -> new HashSet<>())
                    .add(ts.getSubject().getId());
            }
        }

        Map<UUID, Integer> todayLoad = new HashMap<>();
        for (TimetableSlot ts : allSlots) {
            if (ts.getTeacher() != null) todayLoad.merge(ts.getTeacher().getId(), 1, Integer::sum);
        }

        UUID targetSubjectId = sa.getSubject() != null ? sa.getSubject().getId() : null;

        List<User> allTeachers = userRepo.findAllBySchoolIdAndRole(schoolId, Role.TEACHER)
            .stream().filter(User::isActive)
            .filter(t -> !t.getId().equals(sa.getAbsentTeacher().getId()))
            .toList();

        List<SubstituteSuggestionDto> suggestions = new ArrayList<>();

        for (User candidate : allTeachers) {
            if (busyAtPeriod.contains(candidate.getId())) continue;

            float score = 0;
            boolean sameSubject = false;
            List<String> reasons = new ArrayList<>();
            reasons.add("Free period " + sa.getPeriodNo());

            if (targetSubjectId != null) {
                Set<UUID> subs = teacherSubjects.getOrDefault(candidate.getId(), Set.of());
                if (subs.contains(targetSubjectId)) {
                    score += 40;
                    sameSubject = true;
                    reasons.add("teaches " + (sa.getSubject() != null ? sa.getSubject().getName() : "same subject"));
                }
            }

            long subDuties = assignmentRepo.countBySubstituteTeacherIdAndAbsenceDate(
                candidate.getId(), sa.getAbsenceDate());
            score -= subDuties * 10;

            int load = todayLoad.getOrDefault(candidate.getId(), 0);
            score -= load * 5;

            suggestions.add(new SubstituteSuggestionDto(
                candidate.getId(),
                candidate.getFullName(),
                candidate.getDepartment(),
                String.join(" + ", reasons),
                Math.max(0, Math.min(1, (score + 50) / 100f)),
                sameSubject,
                load
            ));
        }

        suggestions.sort(Comparator.comparing(SubstituteSuggestionDto::confidenceScore).reversed());
        return suggestions;
    }

    /**
     * Admin confirms/assigns a substitute, or marks as self-study.
     */
    public SubstituteAssignmentDto assignSubstitute(UUID schoolId, AssignSubstituteRequest req, UUID assignedById) {
        SubstituteAssignment sa = assignmentRepo.findById(req.assignmentId())
            .filter(a -> schoolId.equals(a.getSchoolId()))
            .orElseThrow(() -> new ResourceNotFoundException("SubstituteAssignment", req.assignmentId()));

        User assignedBy = userRepo.findById(assignedById).orElse(null);
        sa.setAssignedBy(assignedBy);
        sa.setRemarks(req.remarks());

        if (req.substituteTeacherId() != null) {
            User sub = userRepo.findByIdAndSchoolId(req.substituteTeacherId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", req.substituteTeacherId()));
            sa.setSubstituteTeacher(sub);
            sa.setStatus("ASSIGNED");
        } else {
            sa.setSubstituteTeacher(null);
            sa.setStatus("SELF_STUDY");
        }

        return toDto(assignmentRepo.save(sa));
    }

    /** Get all assignments for a date */
    @Transactional(readOnly = true)
    public List<SubstituteAssignmentDto> getByDate(UUID schoolId, LocalDate date) {
        return assignmentRepo.findBySchoolIdAndAbsenceDateOrderByPeriodNoAsc(schoolId, date)
            .stream().map(this::toDto).toList();
    }

    /** Get pending assignments for admin review */
    @Transactional(readOnly = true)
    public List<SubstituteAssignmentDto> getPending(UUID schoolId, LocalDate date) {
        return assignmentRepo.findPendingBySchoolAndDate(schoolId, date)
            .stream().map(this::toDto).toList();
    }

    // ── Helpers ──────────────────────────────────────────────

    private String toDayCode(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        return dow.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase().substring(0, 3);
    }

    private SubstituteAssignmentDto toDto(SubstituteAssignment sa) {
        User absent = sa.getAbsentTeacher();
        User sub    = sa.getSubstituteTeacher();
        SchoolClass sc = sa.getSchoolClass();
        Subject subj = sa.getSubject();

        return new SubstituteAssignmentDto(
            sa.getId(),
            absent != null ? absent.getId()     : null,
            absent != null ? absent.getFullName(): null,
            sub    != null ? sub.getId()         : null,
            sub    != null ? sub.getFullName()   : null,
            sa.getAbsenceDate(),
            sa.getPeriodNo(),
            sc     != null ? sc.getId()          : null,
            sc     != null ? sc.getName()        : null,
            subj   != null ? subj.getId()        : null,
            subj   != null ? subj.getName()      : null,
            sa.getStartTime(),
            sa.getEndTime(),
            sa.getStatus(),
            sa.getSuggestionReason(),
            sa.getConfidenceScore(),
            sa.getRemarks()
        );
    }
}
