package com.sms.api.service;

import com.sms.api.dto.timetable.TimetableSlotDto;
import com.sms.api.dto.timetable.UpsertSlotRequest;
import com.sms.api.entity.*;
import com.sms.api.repository.*;
import com.sms.core.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TimetableService {

    private final TimetableSlotRepository slotRepository;
    private final SchoolClassRepository   classRepository;
    private final AcademicYearRepository  yearRepository;
    private final SubjectRepository       subjectRepository;
    private final UserRepository          userRepository;

    public TimetableService(TimetableSlotRepository slotRepository,
                             SchoolClassRepository classRepository,
                             AcademicYearRepository yearRepository,
                             SubjectRepository subjectRepository,
                             UserRepository userRepository) {
        this.slotRepository    = slotRepository;
        this.classRepository   = classRepository;
        this.yearRepository    = yearRepository;
        this.subjectRepository = subjectRepository;
        this.userRepository    = userRepository;
    }

    @Transactional(readOnly = true)
    public List<TimetableSlotDto> getClassTimetable(UUID schoolId, UUID classId, UUID academicYearId) {
        classRepository.findByIdAndSchoolId(classId, schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("Class", classId));
        return slotRepository
            .findBySchoolClassIdAndAcademicYearIdOrderByDayOfWeekAscPeriodNoAsc(classId, academicYearId)
            .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<TimetableSlotDto> getTeacherTimetable(UUID schoolId, UUID teacherId, UUID academicYearId) {
        return slotRepository
            .findByTeacherIdAndAcademicYearIdOrderByDayOfWeekAscPeriodNoAsc(teacherId, academicYearId)
            .stream().map(this::toDto).toList();
    }

    public TimetableSlotDto upsertSlot(UUID schoolId, UpsertSlotRequest req) {
        SchoolClass schoolClass = classRepository.findByIdAndSchoolId(req.classId(), schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("Class", req.classId()));

        AcademicYear academicYear = yearRepository.findByIdAndSchoolId(req.academicYearId(), schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", req.academicYearId()));

        // Upsert — find existing slot for this class/day/period or create new
        TimetableSlot slot = slotRepository
            .findBySchoolClassIdAndDayOfWeekAndPeriodNo(req.classId(), req.dayOfWeek(), req.periodNo())
            .orElse(new TimetableSlot());

        slot.setSchoolId(schoolId);
        slot.setSchoolClass(schoolClass);
        slot.setAcademicYear(academicYear);
        slot.setDayOfWeek(req.dayOfWeek().toUpperCase());
        slot.setPeriodNo((short) req.periodNo());
        slot.setStartTime(req.startTime());
        slot.setEndTime(req.endTime());
        slot.setRoomNo(req.roomNo());

        if (req.subjectId() != null) {
            Subject subject = subjectRepository.findById(req.subjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", req.subjectId()));
            slot.setSubject(subject);
        } else {
            slot.setSubject(null);
        }

        if (req.teacherId() != null) {
            User teacher = userRepository.findById(req.teacherId())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", req.teacherId()));
            slot.setTeacher(teacher);
        } else {
            slot.setTeacher(null);
        }

        return toDto(slotRepository.save(slot));
    }

    public void deleteSlot(UUID schoolId, UUID slotId) {
        TimetableSlot slot = slotRepository.findById(slotId)
            .filter(s -> schoolId.equals(s.getSchoolId()))
            .orElseThrow(() -> new ResourceNotFoundException("TimetableSlot", slotId));
        slotRepository.delete(slot);
    }

    private TimetableSlotDto toDto(TimetableSlot s) {
        SchoolClass sc = s.getSchoolClass();
        Subject     sub = s.getSubject();
        User        t   = s.getTeacher();
        AcademicYear ay = s.getAcademicYear();
        return new TimetableSlotDto(
            s.getId(),
            sc  != null ? sc.getId()        : null,
            sc  != null ? sc.getName()      : null,
            ay  != null ? ay.getId()        : null,
            s.getDayOfWeek(),
            s.getPeriodNo(),
            sub != null ? sub.getId()       : null,
            sub != null ? sub.getName()     : null,
            t   != null ? t.getId()         : null,
            t   != null ? t.getFullName()   : null,
            s.getStartTime(),
            s.getEndTime(),
            s.getRoomNo()
        );
    }
}
