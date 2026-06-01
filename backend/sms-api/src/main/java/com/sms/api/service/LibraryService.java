package com.sms.api.service;

import com.sms.api.dto.library.*;
import com.sms.api.entity.*;
import com.sms.api.repository.*;
import com.sms.core.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class LibraryService {

    private final BookRepository      bookRepository;
    private final BookIssueRepository issueRepository;
    private final StudentRepository   studentRepository;

    public LibraryService(BookRepository bookRepository,
                          BookIssueRepository issueRepository,
                          StudentRepository studentRepository) {
        this.bookRepository   = bookRepository;
        this.issueRepository  = issueRepository;
        this.studentRepository = studentRepository;
    }

    // ── Books ─────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<BookDto> listBooks(UUID schoolId) {
        return bookRepository.findBySchoolIdOrderByTitleAsc(schoolId)
            .stream().map(this::toBookDto).toList();
    }

    public BookDto addBook(UUID schoolId, CreateBookRequest req) {
        Book b = new Book();
        b.setSchoolId(schoolId);
        b.setTitle(req.title());
        b.setAuthor(req.author());
        b.setIsbn(req.isbn());
        b.setCategory(req.category());
        b.setTotalCopies(req.totalCopies());
        b.setAvailableCopies(req.totalCopies());
        return toBookDto(bookRepository.save(b));
    }

    public void deleteBook(UUID schoolId, UUID bookId) {
        Book b = bookRepository.findByIdAndSchoolId(bookId, schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("Book", bookId));
        bookRepository.delete(b);
    }

    // ── Issues ────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<BookIssueDto> listIssued(UUID schoolId) {
        return issueRepository.findBySchoolIdAndIsReturnedFalseOrderByDueDateAsc(schoolId)
            .stream().map(this::toIssueDto).toList();
    }

    public BookIssueDto issueBook(UUID schoolId, IssueBookRequest req) {
        Book book = bookRepository.findByIdAndSchoolId(req.bookId(), schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("Book", req.bookId()));

        if (book.getAvailableCopies() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No copies available for: " + book.getTitle());
        }

        BookIssue issue = new BookIssue();
        issue.setSchoolId(schoolId);
        issue.setBook(book);
        issue.setIssueDate(req.issueDate());
        issue.setDueDate(req.dueDate());

        if (req.studentId() != null) {
            Student s = studentRepository.findByIdAndSchoolId(req.studentId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", req.studentId()));
            issue.setStudent(s);
            issue.setBorrowerName(s.getFirstName() + " " + s.getLastName());
        } else {
            issue.setBorrowerName(req.borrowerName());
        }

        // Decrement available copies
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        return toIssueDto(issueRepository.save(issue));
    }

    public BookIssueDto returnBook(UUID schoolId, UUID issueId) {
        BookIssue issue = issueRepository.findByIdAndSchoolId(issueId, schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("BookIssue", issueId));

        if (issue.isReturned()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Book already returned");
        }

        issue.setReturned(true);
        issue.setReturnDate(LocalDate.now());

        // Increment available copies
        Book book = issue.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        return toIssueDto(issueRepository.save(issue));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private BookDto toBookDto(Book b) {
        return new BookDto(b.getId(), b.getTitle(), b.getAuthor(), b.getIsbn(),
            b.getCategory(), b.getTotalCopies(), b.getAvailableCopies());
    }

    private BookIssueDto toIssueDto(BookIssue i) {
        Student s = i.getStudent();
        boolean overdue = !i.isReturned() && i.getDueDate().isBefore(LocalDate.now());
        return new BookIssueDto(
            i.getId(),
            i.getBook().getId(), i.getBook().getTitle(),
            s != null ? s.getId() : null,
            i.getBorrowerName(),
            i.getIssueDate(), i.getDueDate(), i.getReturnDate(),
            i.isReturned(), overdue
        );
    }
}
