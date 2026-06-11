package com.sms.api.entity;

import com.sms.api.entity.base.SchoolScopedEntity;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "substitute_assignments", indexes = {
    @Index(name = "idx_sub_school_date", columnList = "school_id, absence_date"),
    @Index(name = "idx_sub_absent",      columnList = "absent_teacher_id, absence_date"),
    @Index(name = "idx_sub_substitute",  columnList = "substitute_teacher_id, absence_date")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uq_sub_class_period_date", columnNames = {"class_id", "period_no", "absence_date"})
})
public class SubstituteAssignment extends SchoolScopedEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "absent_teacher_id", nullable = false)
    private User absentTeacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "substitute_teacher_id")
    private User substituteTeacher;

    @Column(name = "absence_date", nullable = false)
    private LocalDate absenceDate;

    @Column(name = "period_no", nullable = false)
    private short periodNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private SchoolClass schoolClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    /** PENDING | SUGGESTED | ASSIGNED | SELF_STUDY | CANCELLED */
    @Column(nullable = false, length = 15)
    private String status = "PENDING";

    @Column(name = "suggestion_reason", length = 500)
    private String suggestionReason;

    @Column(name = "confidence_score")
    private Float confidenceScore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by")
    private User assignedBy;

    @Column(length = 500)
    private String remarks;

    // ── Getters & Setters ────────────────────────────────────
    public User getAbsentTeacher() { return absentTeacher; }
    public void setAbsentTeacher(User absentTeacher) { this.absentTeacher = absentTeacher; }

    public User getSubstituteTeacher() { return substituteTeacher; }
    public void setSubstituteTeacher(User substituteTeacher) { this.substituteTeacher = substituteTeacher; }

    public LocalDate getAbsenceDate() { return absenceDate; }
    public void setAbsenceDate(LocalDate absenceDate) { this.absenceDate = absenceDate; }

    public short getPeriodNo() { return periodNo; }
    public void setPeriodNo(short periodNo) { this.periodNo = periodNo; }

    public SchoolClass getSchoolClass() { return schoolClass; }
    public void setSchoolClass(SchoolClass schoolClass) { this.schoolClass = schoolClass; }

    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSuggestionReason() { return suggestionReason; }
    public void setSuggestionReason(String suggestionReason) { this.suggestionReason = suggestionReason; }

    public Float getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(Float confidenceScore) { this.confidenceScore = confidenceScore; }

    public User getAssignedBy() { return assignedBy; }
    public void setAssignedBy(User assignedBy) { this.assignedBy = assignedBy; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
