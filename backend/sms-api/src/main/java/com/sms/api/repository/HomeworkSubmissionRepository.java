package com.sms.api.repository;

import com.sms.api.entity.HomeworkSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HomeworkSubmissionRepository extends JpaRepository<HomeworkSubmission, UUID> {

    /** All submissions for a homework assignment — teacher overview */
    List<HomeworkSubmission> findByHomeworkIdOrderBySubmittedAtAsc(UUID homeworkId);

    /** Single submission for a specific student + homework pair */
    Optional<HomeworkSubmission> findByHomeworkIdAndStudentId(UUID homeworkId, UUID studentId);

    /** All submissions by a student (their homework history) */
    List<HomeworkSubmission> findByStudentIdOrderByCreatedAtDesc(UUID studentId);

    /** Count by status — e.g. how many are still PENDING for a homework */
    long countByHomeworkIdAndStatus(UUID homeworkId, String status);

    /** All submissions for a student in a school (for parent portal) */
    List<HomeworkSubmission> findBySchoolIdAndStudentIdOrderByCreatedAtDesc(UUID schoolId, UUID studentId);

    /** Check if a submission already exists (prevents double-submit) */
    boolean existsByHomeworkIdAndStudentId(UUID homeworkId, UUID studentId);

    /** Graded submissions for a student — for report card / progress view */
    @Query("SELECT s FROM HomeworkSubmission s WHERE s.student.id = :studentId " +
           "AND s.status = 'GRADED' ORDER BY s.gradedAt DESC")
    List<HomeworkSubmission> findGradedByStudent(UUID studentId);
}
