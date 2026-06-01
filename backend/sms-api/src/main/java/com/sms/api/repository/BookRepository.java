package com.sms.api.repository;

import com.sms.api.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID> {
    List<Book> findBySchoolIdOrderByTitleAsc(UUID schoolId);
    Optional<Book> findByIdAndSchoolId(UUID id, UUID schoolId);
    long countBySchoolId(UUID schoolId);
    long countBySchoolIdAndAvailableCopiesGreaterThan(UUID schoolId, int threshold);
}
