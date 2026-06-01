package com.sms.api.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "book_issues", indexes = {
    @Index(name = "idx_book_issue_school", columnList = "school_id"),
    @Index(name = "idx_book_issue_book",   columnList = "book_id")
})
public class BookIssue {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @Column(name = "borrower_name", length = 200)
    private String borrowerName;

    @Column(nullable = false)
    private LocalDate issueDate;

    @Column(nullable = false)
    private LocalDate dueDate;

    private LocalDate returnDate;

    @Column(nullable = false)
    private boolean isReturned = false;

    @Column(updatable = false)
    private Instant createdAt = Instant.now();

    // ── Getters & Setters ─────────────────────────────────────────────────────
    public UUID getId()                    { return id; }
    public UUID getSchoolId()              { return schoolId; }
    public void setSchoolId(UUID s)        { schoolId = s; }
    public Book getBook()                  { return book; }
    public void setBook(Book b)            { book = b; }
    public Student getStudent()            { return student; }
    public void setStudent(Student s)      { student = s; }
    public String getBorrowerName()        { return borrowerName; }
    public void setBorrowerName(String n)  { borrowerName = n; }
    public LocalDate getIssueDate()        { return issueDate; }
    public void setIssueDate(LocalDate d)  { issueDate = d; }
    public LocalDate getDueDate()          { return dueDate; }
    public void setDueDate(LocalDate d)    { dueDate = d; }
    public LocalDate getReturnDate()       { return returnDate; }
    public void setReturnDate(LocalDate d) { returnDate = d; }
    public boolean isReturned()            { return isReturned; }
    public void setReturned(boolean r)     { isReturned = r; }
    public Instant getCreatedAt()          { return createdAt; }
}
