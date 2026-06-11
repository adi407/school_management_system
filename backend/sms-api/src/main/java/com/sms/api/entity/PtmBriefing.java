package com.sms.api.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ptm_briefings", indexes = {
    @Index(name = "idx_brief_ptm",     columnList = "ptm_meeting_id"),
    @Index(name = "idx_brief_student", columnList = "student_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uq_brief_ptm_student", columnNames = {"ptm_meeting_id", "student_id"})
})
@EntityListeners(AuditingEntityListener.class)
public class PtmBriefing {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ptm_meeting_id", nullable = false)
    private PtmMeeting ptmMeeting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private User teacher;

    // ── Aggregated stats (snapshot at generation time) ────────
    @Column(name = "attendance_pct")
    private Float attendancePct;

    @Column(name = "avg_marks")
    private Float avgMarks;

    @Column(name = "homework_completion_pct")
    private Float homeworkCompletionPct;

    /** IMPROVING | STABLE | DECLINING */
    @Column(name = "wellness_trend", length = 20)
    private String wellnessTrend;

    // ── AI-generated content ─────────────────────────────────
    @Column(name = "ai_summary", columnDefinition = "TEXT")
    private String aiSummary;

    @Column(name = "talking_points", columnDefinition = "TEXT")
    private String talkingPoints;

    @Column(name = "parent_preview", columnDefinition = "TEXT")
    private String parentPreview;

    /** DRAFT | REVIEWED | SENT_TO_PARENT */
    @Column(nullable = false, length = 20)
    private String status = "DRAFT";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    // ── Getters & Setters ────────────────────────────────────
    public UUID getId() { return id; }

    public PtmMeeting getPtmMeeting() { return ptmMeeting; }
    public void setPtmMeeting(PtmMeeting ptmMeeting) { this.ptmMeeting = ptmMeeting; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public User getTeacher() { return teacher; }
    public void setTeacher(User teacher) { this.teacher = teacher; }

    public Float getAttendancePct() { return attendancePct; }
    public void setAttendancePct(Float attendancePct) { this.attendancePct = attendancePct; }

    public Float getAvgMarks() { return avgMarks; }
    public void setAvgMarks(Float avgMarks) { this.avgMarks = avgMarks; }

    public Float getHomeworkCompletionPct() { return homeworkCompletionPct; }
    public void setHomeworkCompletionPct(Float homeworkCompletionPct) { this.homeworkCompletionPct = homeworkCompletionPct; }

    public String getWellnessTrend() { return wellnessTrend; }
    public void setWellnessTrend(String wellnessTrend) { this.wellnessTrend = wellnessTrend; }

    public String getAiSummary() { return aiSummary; }
    public void setAiSummary(String aiSummary) { this.aiSummary = aiSummary; }

    public String getTalkingPoints() { return talkingPoints; }
    public void setTalkingPoints(String talkingPoints) { this.talkingPoints = talkingPoints; }

    public String getParentPreview() { return parentPreview; }
    public void setParentPreview(String parentPreview) { this.parentPreview = parentPreview; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public User getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(User reviewedBy) { this.reviewedBy = reviewedBy; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
