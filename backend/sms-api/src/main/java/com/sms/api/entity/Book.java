package com.sms.api.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "books", indexes = { @Index(name = "idx_book_school", columnList = "school_id") })
public class Book {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(length = 200)
    private String author;

    @Column(length = 30)
    private String isbn;

    @Column(length = 100)
    private String category;

    @Column(nullable = false)
    private int totalCopies = 1;

    @Column(nullable = false)
    private int availableCopies = 1;

    @Column(updatable = false)
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    @PreUpdate void touch() { updatedAt = Instant.now(); }

    // ── Getters & Setters ─────────────────────────────────────────────────────
    public UUID getId()                   { return id; }
    public UUID getSchoolId()             { return schoolId; }
    public void setSchoolId(UUID s)       { schoolId = s; }
    public String getTitle()              { return title; }
    public void setTitle(String t)        { title = t; }
    public String getAuthor()             { return author; }
    public void setAuthor(String a)       { author = a; }
    public String getIsbn()               { return isbn; }
    public void setIsbn(String i)         { isbn = i; }
    public String getCategory()           { return category; }
    public void setCategory(String c)     { category = c; }
    public int getTotalCopies()           { return totalCopies; }
    public void setTotalCopies(int n)     { totalCopies = n; }
    public int getAvailableCopies()       { return availableCopies; }
    public void setAvailableCopies(int n) { availableCopies = n; }
    public Instant getCreatedAt()         { return createdAt; }
    public Instant getUpdatedAt()         { return updatedAt; }
}
