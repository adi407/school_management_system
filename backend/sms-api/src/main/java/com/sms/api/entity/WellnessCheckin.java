package com.sms.api.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Campus Pulse — anonymous daily mood check-in.
 * student_id is nullable for fully anonymous mode.
 */
@Entity
@Table(name = "wellness_checkins", indexes = {
    @Index(name = "idx_wellness_class_date",  columnList = "class_id, checkin_date"),
    @Index(name = "idx_wellness_school_date", columnList = "school_id, checkin_date")
})
public class WellnessCheckin {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private SchoolClass schoolClass;

    @Column(name = "academic_year_id")
    private UUID academicYearId;

    @Column(nullable = false)
    private LocalDate checkinDate = LocalDate.now();

    /** GREAT | GOOD | OKAY | SAD | STRESSED */
    @Column(nullable = false, length = 10)
    private String mood;

    /** NULL = anonymous; populated if student opts in to tracking */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @Column(length = 200)
    private String note;

    @Column(updatable = false)
    private Instant createdAt = Instant.now();

    // ── Getters & Setters ─────────────────────────────────────
    public UUID getId() { return id; }
    public UUID getSchoolId() { return schoolId; }
    public void setSchoolId(UUID schoolId) { this.schoolId = schoolId; }
    public SchoolClass getSchoolClass() { return schoolClass; }
    public void setSchoolClass(SchoolClass schoolClass) { this.schoolClass = schoolClass; }
    public UUID getAcademicYearId() { return academicYearId; }
    public void setAcademicYearId(UUID academicYearId) { this.academicYearId = academicYearId; }
    public LocalDate getCheckinDate() { return checkinDate; }
    public void setCheckinDate(LocalDate checkinDate) { this.checkinDate = checkinDate; }
    public String getMood() { return mood; }
    public void setMood(String mood) { this.mood = mood; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public Instant getCreatedAt() { return createdAt; }
}
