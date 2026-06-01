package com.sms.api.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "exams", indexes = {
    @Index(name = "idx_exam_school", columnList = "school_id"),
    @Index(name = "idx_exam_class",  columnList = "class_id")
})
public class Exam {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id")
    private AcademicYear academicYear;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private SchoolClass schoolClass;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 50)
    private String examType;  // UNIT_TEST | MIDTERM | PREBOARD | ANNUAL

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private int totalSubjects = 0;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 20)
    private String status = "UPCOMING";  // UPCOMING | ONGOING | COMPLETED

    @Column(updatable = false)
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    @PreUpdate
    void touch() { updatedAt = Instant.now(); }

    // ── Getters & Setters ─────────────────────────────────────────────────────
    public UUID getId()                    { return id; }
    public UUID getSchoolId()              { return schoolId; }
    public void setSchoolId(UUID s)        { schoolId = s; }
    public AcademicYear getAcademicYear()  { return academicYear; }
    public void setAcademicYear(AcademicYear ay) { academicYear = ay; }
    public SchoolClass getSchoolClass()    { return schoolClass; }
    public void setSchoolClass(SchoolClass sc) { schoolClass = sc; }
    public String getName()                { return name; }
    public void setName(String n)          { name = n; }
    public String getExamType()            { return examType; }
    public void setExamType(String t)      { examType = t; }
    public LocalDate getStartDate()        { return startDate; }
    public void setStartDate(LocalDate d)  { startDate = d; }
    public LocalDate getEndDate()          { return endDate; }
    public void setEndDate(LocalDate d)    { endDate = d; }
    public int getTotalSubjects()          { return totalSubjects; }
    public void setTotalSubjects(int n)    { totalSubjects = n; }
    public String getDescription()         { return description; }
    public void setDescription(String d)   { description = d; }
    public String getStatus()              { return status; }
    public void setStatus(String s)        { status = s; }
    public Instant getCreatedAt()          { return createdAt; }
    public Instant getUpdatedAt()          { return updatedAt; }
}
