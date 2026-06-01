package com.sms.api.service;

import com.sms.api.dto.academic.AssignSubjectRequest;
import com.sms.api.dto.academic.ClassDto;
import com.sms.api.dto.academic.ClassSubjectDto;
import com.sms.api.dto.academic.CreateClassRequest;
import com.sms.api.entity.ClassSubjectTeacher;
import com.sms.api.entity.School;
import com.sms.api.entity.SchoolClass;
import com.sms.api.entity.Subject;
import com.sms.api.entity.User;
import com.sms.api.repository.ClassSubjectTeacherRepository;
import com.sms.api.repository.SchoolClassRepository;
import com.sms.api.repository.SchoolRepository;
import com.sms.api.repository.StudentRepository;
import com.sms.api.repository.SubjectRepository;
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
    private final SubjectRepository subjectRepository;
    private final ClassSubjectTeacherRepository cstRepository;

    public ClassService(SchoolClassRepository classRepository,
                        SchoolRepository schoolRepository,
                        StudentRepository studentRepository,
                        UserRepository userRepository,
                        SubjectRepository subjectRepository,
                        ClassSubjectTeacherRepository cstRepository) {
        this.classRepository   = classRepository;
        this.schoolRepository  = schoolRepository;
        this.studentRepository = studentRepository;
        this.userRepository    = userRepository;
        this.subjectRepository = subjectRepository;
        this.cstRepository     = cstRepository;
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

    // ── Subject Assignment ────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ClassSubjectDto> listSubjects(UUID schoolId, UUID classId) {
        findOrThrow(schoolId, classId); // ensure class belongs to school
        return cstRepository.findBySchoolIdAndClassId(schoolId, classId)
            .stream().map(this::toCstDto).toList();
    }

    public ClassSubjectDto assignSubject(UUID schoolId, UUID classId, AssignSubjectRequest req) {
        SchoolClass sc = findOrThrow(schoolId, classId);

        Subject subject = subjectRepository.findByIdAndSchoolId(req.subjectId(), schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("Subject", req.subjectId()));

        if (cstRepository.existsBySchoolIdAndSchoolClassIdAndSubjectId(schoolId, classId, req.subjectId())) {
            throw new DuplicateResourceException("Subject '" + subject.getName() + "' is already assigned to this class");
        }

        ClassSubjectTeacher cst = new ClassSubjectTeacher();
        cst.setSchoolId(schoolId);
        cst.setSchoolClass(sc);
        cst.setSubject(subject);

        if (req.teacherId() != null) {
            User teacher = userRepository.findById(req.teacherId())
                .orElseThrow(() -> new ResourceNotFoundException("User", req.teacherId()));
            cst.setTeacher(teacher);
        }

        return toCstDto(cstRepository.save(cst));
    }

    public ClassSubjectDto updateSubjectTeacher(UUID schoolId, UUID classId, UUID cstId, UUID teacherId) {
        ClassSubjectTeacher cst = cstRepository.findByIdAndSchoolId(cstId, schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("ClassSubjectTeacher", cstId));

        if (teacherId != null) {
            User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("User", teacherId));
            cst.setTeacher(teacher);
        } else {
            cst.setTeacher(null);
        }

        return toCstDto(cstRepository.save(cst));
    }

    public void removeSubject(UUID schoolId, UUID classId, UUID cstId) {
        ClassSubjectTeacher cst = cstRepository.findByIdAndSchoolId(cstId, schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("ClassSubjectTeacher", cstId));
        cstRepository.delete(cst);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private SchoolClass findOrThrow(UUID schoolId, UUID id) {
        return classRepository.findByIdAndSchoolId(id, schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("Class", id));
    }

    private ClassDto toDto(SchoolClass c, long studentCount) {
        User teacher = c.getClassTeacher();
        return new ClassDto(
            c.getId(), c.getSchoolId(), c.getName(), c.getGrade(), c.getSection(),
            c.getCapacity(), studentCount,
            teacher != null ? teacher.getId()       : null,
            teacher != null ? teacher.getFullName() : null,
            c.getCreatedAt()
        );
    }

    private ClassSubjectDto toCstDto(ClassSubjectTeacher cst) {
        Subject s = cst.getSubject();
        User    t = cst.getTeacher();
        return new ClassSubjectDto(
            cst.getId(),
            cst.getSchoolClass().getId(),
            s.getId(), s.getName(), s.getCode(), s.getType(), s.getCreditHours(),
            t != null ? t.getId()          : null,
            t != null ? t.getFullName()    : null,
            t != null ? t.getEmail()       : null
        );
    }
}
