package com.sms.api.repository;

import com.sms.api.entity.ClassSubjectTeacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClassSubjectTeacherRepository extends JpaRepository<ClassSubjectTeacher, UUID> {

    @Query("SELECT cst FROM ClassSubjectTeacher cst " +
           "JOIN FETCH cst.subject " +
           "LEFT JOIN FETCH cst.teacher " +
           "WHERE cst.schoolId = :schoolId AND cst.schoolClass.id = :classId")
    List<ClassSubjectTeacher> findBySchoolIdAndClassId(UUID schoolId, UUID classId);

    boolean existsBySchoolIdAndSchoolClassIdAndSubjectId(UUID schoolId, UUID classId, UUID subjectId);

    Optional<ClassSubjectTeacher> findByIdAndSchoolId(UUID id, UUID schoolId);

    long countBySchoolIdAndSchoolClassId(UUID schoolId, UUID classId);
}
