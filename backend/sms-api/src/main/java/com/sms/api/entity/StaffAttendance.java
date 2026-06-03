package com.sms.api.entity;

import com.sms.api.entity.base.SchoolScopedEntity;
import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * Daily attendance record for staff members (teachers, admins, accountants, etc.)
 * Used by the payroll engine to compute Loss-of-Pay (LOP) deductions.
 */
@Entity
@Table(name = "staff_attendance", indexes = {
    @Index(name = "idx_satt_school",    columnList = "school_id"),
    @Index(name = "idx_satt_user_date", columnList = "user_id, attendance_date")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uq_satt_user_date", columnNames = {"user_id", "attendance_date"})
})
public class StaffAttendance extends SchoolScopedEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User staff;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    /** PRESENT | ABSENT | HALF_DAY | HOLIDAY | LEAVE */
    @Column(nullable = false, length = 12)
    private String status;

    @Column(length = 500)
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marked_by")
    private User markedBy;

    public User getStaff() { return staff; }
    public void setStaff(User staff) { this.staff = staff; }

    public LocalDate getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public User getMarkedBy() { return markedBy; }
    public void setMarkedBy(User markedBy) { this.markedBy = markedBy; }
}
