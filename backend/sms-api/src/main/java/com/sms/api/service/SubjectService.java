package com.sms.api.service;

import com.sms.api.dto.academic.CreateSubjectRequest;
import com.sms.api.dto.academic.SubjectDto;
import com.sms.api.entity.School;
import com.sms.api.entity.Subject;
import com.sms.api.repository.SchoolRepository;
import com.sms.api.repository.SubjectRepository;
import com.sms.core.exception.DuplicateResourceException;
import com.sms.core.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final SchoolRepository  schoolRepository;

    public SubjectService(SubjectRepository subjectRepository,
                          SchoolRepository schoolRepository) {
        this.subjectRepository = subjectRepository;
        this.schoolRepository  = schoolRepository;
    }

    @Transactional(readOnly = true)
    public List<SubjectDto> list(UUID schoolId) {
        return subjectRepository.findBySchoolIdOrderByNameAsc(schoolId)
            .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public SubjectDto get(UUID schoolId, UUID id) {
        return toDto(findOrThrow(schoolId, id));
    }

    public SubjectDto create(UUID schoolId, CreateSubjectRequest req) {
        School school = schoolRepository.findById(schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("School", schoolId));

        if (subjectRepository.existsBySchoolIdAndCode(schoolId, req.code())) {
            throw new DuplicateResourceException(
                "Subject with code '" + req.code() + "' already exists");
        }

        Subject s = new Subject();
        s.setSchool(school);
        s.setName(req.name());
        s.setCode(req.code().toUpperCase());
        s.setType(req.type() != null ? req.type().toUpperCase() : "CORE");
        s.setCreditHours(req.creditHours());

        return toDto(subjectRepository.save(s));
    }

    public SubjectDto update(UUID schoolId, UUID id, CreateSubjectRequest req) {
        Subject s = findOrThrow(schoolId, id);

        if (req.name()        != null) s.setName(req.name());
        if (req.type()        != null) s.setType(req.type().toUpperCase());
        if (req.creditHours() != null) s.setCreditHours(req.creditHours());

        // Code change: check for duplicate only if changing to a different code
        if (req.code() != null && !req.code().equalsIgnoreCase(s.getCode())) {
            if (subjectRepository.existsBySchoolIdAndCode(schoolId, req.code())) {
                throw new DuplicateResourceException(
                    "Subject with code '" + req.code() + "' already exists");
            }
            s.setCode(req.code().toUpperCase());
        }

        return toDto(subjectRepository.save(s));
    }

    public void delete(UUID schoolId, UUID id) {
        Subject s = findOrThrow(schoolId, id);
        subjectRepository.delete(s);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Subject findOrThrow(UUID schoolId, UUID id) {
        return subjectRepository.findByIdAndSchoolId(id, schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("Subject", id));
    }

    private SubjectDto toDto(Subject s) {
        return new SubjectDto(
            s.getId(), s.getSchoolId(),
            s.getName(), s.getCode(), s.getType(), s.getCreditHours(),
            s.getCreatedAt()
        );
    }
}
