package com.sms.api.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "homework_submissions",
    uniqueConstraints = @UniqueConstraint(columnNames = {"homework_id", "student_id"}),
    indexes = {
        @Index(name = "idx_hw_sub_homework", columnList = "homework_id"),
        @Index(name = "idx_hw_sub_student",  columnList = "student_id")
    })
public class HomeworkSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "homework_id", nullable = false)
    private Homework homework;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    private Instant submittedAt;

    /** PENDING | SUBMITTED | LATE | GRADED */
    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(columnDefinition = "TEXT")
    private String content;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String attachments = "[]";

    @Column(length = 500)
    private String remarks;

    @Column(length = 5)
    private String grade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "graded_by")
    private User gradedBy;

    private Instant gradedAt;

    @Column(updatable = false)
    private Instant createdAt = Instant.now();

    // ── Getters & Setters ─────────────────────────────────────
    public UUID getId() { return id; }
    public Homework getHomework() { return homework; }
    public void setHomework(Homework homework) { this.homework = homework; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public UUID getSchoolId() { return schoolId; }
    public void setSchoolId(UUID schoolId) { this.schoolId = schoolId; }
    public Instant getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getAttachments() { return attachments; }
    public void setAttachments(String attachments) { this.attachments = attachments; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public User getGradedBy() { return gradedBy; }
    public void setGradedBy(User gradedBy) { this.gradedBy = gradedBy; }
    public Instant getGradedAt() { return gradedAt; }
    public void setGradedAt(Instant gradedAt) { this.gradedAt = gradedAt; }
    public Instant getCreatedAt() { return createdAt; }
}
