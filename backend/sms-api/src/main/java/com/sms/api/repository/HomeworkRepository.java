package com.sms.api.repository;

import com.sms.api.entity.Homework;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HomeworkRepository extends JpaRepository<Homework, UUID> {

    Optional<Homework> findByIdAndSchoolId(UUID id, UUID schoolId);

    /** Admin / Teacher: paginated list with optional filters */
    @Query("SELECT h FROM Homework h WHERE h.schoolId = :schoolId " +
           "AND (:classId IS NULL OR h.schoolClass.id = :classId) " +
           "AND (:subjectId IS NULL OR h.subject.id = :subjectId) " +
           "AND (:from IS NULL OR h.dueDate >= :from) " +
           "AND (:to IS NULL OR h.dueDate <= :to) " +
           "AND h.isPublished = true " +
           "ORDER BY h.dueDate DESC")
    Page<Homework> findFiltered(UUID schoolId, UUID classId, UUID subjectId,
                                LocalDate from, LocalDate to, Pageable pageable);

    /** Parent / Student: upcoming homework for a specific class */
    @Query("SELECT h FROM Homework h WHERE h.schoolClass.id = :classId " +
           "AND h.dueDate >= :today AND h.isPublished = true " +
           "ORDER BY h.dueDate ASC")
    List<Homework> findUpcomingForClass(UUID classId, LocalDate today);

    /** All homework for a class in a date range — for parent feed */
    @Query("SELECT h FROM Homework h WHERE h.schoolClass.id = :classId " +
           "AND h.dueDate BETWEEN :from AND :to AND h.isPublished = true " +
           "ORDER BY h.dueDate DESC")
    List<Homework> findForClassInRange(UUID classId, LocalDate from, LocalDate to);
}
