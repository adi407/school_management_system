package com.sms.api.service;

import com.sms.api.dto.wellness.ClassPulseDto;
import com.sms.api.dto.wellness.WellnessCheckinRequest;
import com.sms.api.entity.SchoolClass;
import com.sms.api.entity.Student;
import com.sms.api.entity.WellnessCheckin;
import com.sms.api.repository.SchoolClassRepository;
import com.sms.api.repository.StudentRepository;
import com.sms.api.repository.WellnessCheckinRepository;
import com.sms.core.exception.DuplicateResourceException;
import com.sms.core.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Campus Pulse — the USP wellness check-in feature.
 *
 * Students do a one-tap daily mood check-in (GREAT / GOOD / OKAY / SAD / STRESSED).
 * The service aggregates moods per class and flags counselors when ≥30% of
 * check-ins in a class are negative (SAD or STRESSED).
 */
@Service
@Transactional
public class WellnessService {

    /** Alert threshold: % of negative moods that triggers a counselor notification */
    private static final double ALERT_THRESHOLD = 0.30;

    private static final Set<String> NEGATIVE_MOODS = Set.of("SAD", "STRESSED");
    private static final Set<String> POSITIVE_MOODS = Set.of("GREAT", "GOOD");

    private final WellnessCheckinRepository checkinRepository;
    private final SchoolClassRepository     classRepository;
    private final StudentRepository         studentRepository;

    public WellnessService(WellnessCheckinRepository checkinRepository,
                           SchoolClassRepository classRepository,
                           StudentRepository studentRepository) {
        this.checkinRepository = checkinRepository;
        this.classRepository   = classRepository;
        this.studentRepository = studentRepository;
    }

    // ── Student: submit today's mood ──────────────────────────────────────────

    public void checkin(UUID schoolId, UUID studentId, WellnessCheckinRequest req) {
        LocalDate today = LocalDate.now();

        SchoolClass schoolClass = classRepository.findByIdAndSchoolId(req.classId(), schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("Class", req.classId()));

        // One check-in per student per day (if non-anonymous)
        if (!req.anonymous() && studentId != null) {
            if (checkinRepository.findByStudentIdAndCheckinDate(studentId, today).isPresent()) {
                throw new DuplicateResourceException("You have already checked in today");
            }
        }

        WellnessCheckin wc = new WellnessCheckin();
        wc.setSchoolId(schoolId);
        wc.setSchoolClass(schoolClass);
        wc.setMood(req.mood().toUpperCase());
        wc.setCheckinDate(today);

        if (req.note() != null && req.note().length() <= 200) {
            wc.setNote(req.note());
        }

        // Only link student if they opted in to tracking
        if (!req.anonymous() && studentId != null) {
            Student student = studentRepository.findByIdAndSchoolId(studentId, schoolId)
                .orElse(null);
            if (student != null) wc.setStudent(student);
        }

        checkinRepository.save(wc);
    }

    // ── Teacher / Counselor: aggregate for a class on a date ─────────────────

    @Transactional(readOnly = true)
    public ClassPulseDto getClassPulse(UUID schoolId, UUID classId, LocalDate date) {
        SchoolClass sc = classRepository.findByIdAndSchoolId(classId, schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("Class", classId));

        List<WellnessCheckin> checkins =
            checkinRepository.findBySchoolClassIdAndCheckinDate(classId, date);

        return buildPulse(sc, date, checkins);
    }

    // ── Admin / Counselor: school-wide pulse (all classes) on a date ──────────

    @Transactional(readOnly = true)
    public List<ClassPulseDto> getSchoolPulse(UUID schoolId, LocalDate date) {
        List<SchoolClass> classes = classRepository.findBySchoolIdOrderByGradeAscSectionAsc(schoolId);

        // Group all checkins for the school on this date by classId
        Map<UUID, List<WellnessCheckin>> byClass =
            checkinRepository.findBySchoolIdAndCheckinDate(schoolId, date)
                .stream()
                .collect(Collectors.groupingBy(wc -> wc.getSchoolClass().getId()));

        return classes.stream()
            .map(sc -> buildPulse(sc, date,
                byClass.getOrDefault(sc.getId(), Collections.emptyList())))
            .toList();
    }

    // ── Trend: mood history for a class over N days ───────────────────────────

    @Transactional(readOnly = true)
    public List<ClassPulseDto> getClassTrend(UUID schoolId, UUID classId,
                                              LocalDate from, LocalDate to) {
        SchoolClass sc = classRepository.findByIdAndSchoolId(classId, schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("Class", classId));

        List<WellnessCheckin> checkins = checkinRepository.findClassTrend(classId, from, to);

        // Group by date and build one pulse per day
        Map<LocalDate, List<WellnessCheckin>> byDate = checkins.stream()
            .collect(Collectors.groupingBy(WellnessCheckin::getCheckinDate));

        return byDate.entrySet().stream()
            .sorted(Map.Entry.<LocalDate, List<WellnessCheckin>>comparingByKey().reversed())
            .map(entry -> buildPulse(sc, entry.getKey(), entry.getValue()))
            .toList();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private ClassPulseDto buildPulse(SchoolClass sc, LocalDate date,
                                      List<WellnessCheckin> checkins) {
        int total = checkins.size();

        // Count occurrences of each mood
        Map<String, Long> breakdown = checkins.stream()
            .collect(Collectors.groupingBy(WellnessCheckin::getMood, Collectors.counting()));

        int positive = (int) checkins.stream()
            .filter(c -> POSITIVE_MOODS.contains(c.getMood())).count();
        int neutral  = (int) checkins.stream()
            .filter(c -> "OKAY".equals(c.getMood())).count();
        int negative = (int) checkins.stream()
            .filter(c -> NEGATIVE_MOODS.contains(c.getMood())).count();

        double negativePct   = total > 0 ? (double) negative / total : 0.0;
        boolean alertTriggered = total > 0 && negativePct >= ALERT_THRESHOLD;

        return new ClassPulseDto(
            sc.getId(), sc.getName(), date,
            total, breakdown,
            positive, neutral, negative,
            Math.round(negativePct * 1000.0) / 10.0,   // round to 1 decimal %
            alertTriggered
        );
    }
}
