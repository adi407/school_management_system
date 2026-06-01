package com.sms.api.service;

import com.sms.api.dto.academic.ClassDto;
import com.sms.api.dto.academic.CreateClassRequest;
import com.sms.api.entity.School;
import com.sms.api.entity.SchoolClass;
import com.sms.api.entity.User;
import com.sms.api.repository.SchoolClassRepository;
import com.sms.api.repository.SchoolRepository;
import com.sms.api.repository.StudentRepository;
import com.sms.api.repository.UserRepository;
import com.sms.core.exception.DuplicateResourceException;
import com.sms.core.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ClassService {

    private final SchoolClassRepository classRepository;
    private final SchoolRepository schoolRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    public ClassService(SchoolClassRepository classRepository,
                        SchoolRepository schoolRepository,
                        StudentRepository studentRepository,
                        UserRepository userRepository) {
        this.classRepository  = classRepository;
        this.schoolRepository = schoolRepository;
        this.studentRepository= studentRepository;
        this.userRepository   = userRepository;
    }

    @Transactional(readOnly = true)
    public List<ClassDto> list(UUID schoolId) {
        return classRepository.findBySchoolIdOrderByGradeAscSectionAsc(schoolId)
            .stream().map(c -> toDto(c, studentRepository.countBySchoolClassId(c.getId())))
            .toList();
    }

    @Transactional(readOnly = true)
    public ClassDto get(UUID schoolId, UUID id) {
        SchoolClass c = findOrThrow(schoolId, id);
        return toDto(c, studentRepository.countBySchoolClassId(c.getId()));
    }

    public ClassDto create(UUID schoolId, CreateClassRequest req) {
        School school = schoolRepository.findById(schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("School", schoolId));

        if (classRepository.existsBySchoolIdAndGradeAndSection(schoolId, req.grade(), req.section())) {
            throw new DuplicateResourceException(
                "Class Grade " + req.grade() + " Section " + req.section() + " already exists");
        }

        SchoolClass sc = new SchoolClass();
        sc.setSchool(school);
        sc.setGrade(req.grade());
        sc.setSection(req.section());
        sc.setName(req.name());
        sc.setCapacity(req.capacity() > 0 ? req.capacity() : 40);

        if (req.classTeacherId() != null) {
            User teacher = userRepository.findById(req.classTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("User", req.classTeacherId()));
            sc.setClassTeacher(teacher);
        }

        return toDto(classRepository.save(sc), 0L);
    }

    public ClassDto update(UUID schoolId, UUID id, CreateClassRequest req) {
        SchoolClass sc = findOrThrow(schoolId, id);
        if (req.name()     != null) sc.setName(req.name());
        if (req.capacity() > 0)     sc.setCapacity(req.capacity());

        if (req.classTeacherId() != null) {
            User teacher = userRepository.findById(req.classTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("User", req.classTeacherId()));
            sc.setClassTeacher(teacher);
        }
        return toDto(classRepository.save(sc), studentRepository.countBySchoolClassId(id));
    }

    private SchoolClass findOrThrow(UUID schoolId, UUID id) {
        return classRepository.findByIdAndSchoolId(id, schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("Class", id));
    }

    private ClassDto toDto(SchoolClass c, long studentCount) {
        User teacher = c.getClassTeacher();
        return new ClassDto(
            c.getId(), c.getSchoolId(), c.getName(), c.getGrade(), c.getSection(),
            c.getCapacity(), studentCount,
            teacher != null ? teacher.getId() : null,
            teacher != null ? teacher.getEmail() : null,
            c.getCreatedAt()
        );
    }
}
