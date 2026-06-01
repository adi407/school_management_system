package com.sms.api.service;

import com.sms.api.dto.academic.AcademicYearDto;
import com.sms.api.dto.academic.CreateAcademicYearRequest;
import com.sms.api.entity.AcademicYear;
import com.sms.api.entity.School;
import com.sms.api.repository.AcademicYearRepository;
import com.sms.api.repository.SchoolRepository;
import com.sms.core.exception.ResourceNotFoundException;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AcademicYearService {

    private final AcademicYearRepository academicYearRepository;
    private final SchoolRepository schoolRepository;
    private final EntityManager em;

    public AcademicYearService(AcademicYearRepository academicYearRepository,
                               SchoolRepository schoolRepository,
                               EntityManager em) {
        this.academicYearRepository = academicYearRepository;
        this.schoolRepository       = schoolRepository;
        this.em                     = em;
    }

    @Transactional(readOnly = true)
    public List<AcademicYearDto> list(UUID schoolId) {
        return academicYearRepository.findBySchoolIdOrderByStartDateDesc(schoolId)
            .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public AcademicYearDto get(UUID schoolId, UUID id) {
        return toDto(findOrThrow(schoolId, id));
    }

    public AcademicYearDto create(UUID schoolId, CreateAcademicYearRequest req) {
        School school = schoolRepository.findById(schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("School", schoolId));

        if (req.isCurrent()) {
            // clear existing current flag
            em.createQuery("UPDATE AcademicYear a SET a.isCurrent = false WHERE a.schoolId = :sid")
              .setParameter("sid", schoolId).executeUpdate();
        }

        AcademicYear ay = new AcademicYear();
        ay.setSchool(school);
        ay.setName(req.name());
        ay.setStartDate(req.startDate());
        ay.setEndDate(req.endDate());
        ay.setCurrent(req.isCurrent());
        return toDto(academicYearRepository.save(ay));
    }

    public AcademicYearDto update(UUID schoolId, UUID id, CreateAcademicYearRequest req) {
        AcademicYear ay = findOrThrow(schoolId, id);
        if (req.name()      != null) ay.setName(req.name());
        if (req.startDate() != null) ay.setStartDate(req.startDate());
        if (req.endDate()   != null) ay.setEndDate(req.endDate());

        if (req.isCurrent() && !ay.isCurrent()) {
            em.createQuery("UPDATE AcademicYear a SET a.isCurrent = false WHERE a.schoolId = :sid")
              .setParameter("sid", schoolId).executeUpdate();
            ay.setCurrent(true);
        }
        return toDto(academicYearRepository.save(ay));
    }

    private AcademicYear findOrThrow(UUID schoolId, UUID id) {
        return academicYearRepository.findByIdAndSchoolId(id, schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", id));
    }

    private AcademicYearDto toDto(AcademicYear a) {
        return new AcademicYearDto(
            a.getId(), a.getSchoolId(), a.getName(),
            a.getStartDate(), a.getEndDate(), a.isCurrent(),
            a.getCreatedAt()
        );
    }
}
