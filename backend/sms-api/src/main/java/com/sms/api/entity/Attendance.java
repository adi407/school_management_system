package com.sms.api.entity;

import com.sms.api.entity.base.SchoolScopedEntity;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "attendance", indexes = {
    @Index(name = "idx_att_class_date",   columnList = "class_id, attendance_date"),
    @Index(name = "idx_att_student_date", columnList = "student_id, attendance_date"),
    @Index(name = "idx_att_school",       columnList = "school_id")
},
uniqueConstraints = {
    @UniqueConstraint(name = "uq_att_student_date",
        columnNames = {"student_id", "attendance_date"})
})
public class Attendance extends SchoolScopedEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private SchoolClass schoolClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marked_by", nullable = false)
    private User markedBy;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    /** PRESENT | ABSENT | LATE | HOLIDAY | EXCUSED */
    @Column(nullable = false, length = 10)
    private String status;

    @Column(length = 500)
    private String remarks;

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public SchoolClass getSchoolClass() { return schoolClass; }
    public void setSchoolClass(SchoolClass schoolClass) { this.schoolClass = schoolClass; }
    public User getMarkedBy() { return markedBy; }
    public void setMarkedBy(User markedBy) { this.markedBy = markedBy; }
    public LocalDate getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
