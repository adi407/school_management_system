package com.sms.api.entity;

import com.sms.core.enums.BoardType;
import com.sms.core.enums.SubscriptionTier;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "school_registrations", indexes = {
    @Index(name = "idx_school_reg_status", columnList = "status"),
    @Index(name = "idx_school_reg_email", columnList = "admin_email")
})
@EntityListeners(AuditingEntityListener.class)
public class SchoolRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String schoolName;

    @Column(nullable = false)
    private String schoolCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BoardType board = BoardType.CBSE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionTier requestedTier = SubscriptionTier.FREE;

    private String address;
    private String city;
    private String state;
    private String phone;
    private String schoolEmail;
    private String website;
    private Integer studentCount;

    @Column(nullable = false)
    private String adminName;

    @Column(name = "admin_email", nullable = false)
    private String adminEmail;

    private String adminPhone;
    private String adminDesignation;

    @Column(length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegistrationStatus status = RegistrationStatus.PENDING_APPROVAL;

    @Column(length = 1000)
    private String rejectionReason;

    private UUID approvedSchoolId;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    private Instant reviewedAt;

    public enum RegistrationStatus {
        PENDING_APPROVAL, APPROVED, REJECTED
    }

    // Getters and setters
    public UUID getId() { return id; }
    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }
    public String getSchoolCode() { return schoolCode; }
    public void setSchoolCode(String schoolCode) { this.schoolCode = schoolCode; }
    public BoardType getBoard() { return board; }
    public void setBoard(BoardType board) { this.board = board; }
    public SubscriptionTier getRequestedTier() { return requestedTier; }
    public void setRequestedTier(SubscriptionTier requestedTier) { this.requestedTier = requestedTier; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getSchoolEmail() { return schoolEmail; }
    public void setSchoolEmail(String schoolEmail) { this.schoolEmail = schoolEmail; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public Integer getStudentCount() { return studentCount; }
    public void setStudentCount(Integer studentCount) { this.studentCount = studentCount; }
    public String getAdminName() { return adminName; }
    public void setAdminName(String adminName) { this.adminName = adminName; }
    public String getAdminEmail() { return adminEmail; }
    public void setAdminEmail(String adminEmail) { this.adminEmail = adminEmail; }
    public String getAdminPhone() { return adminPhone; }
    public void setAdminPhone(String adminPhone) { this.adminPhone = adminPhone; }
    public String getAdminDesignation() { return adminDesignation; }
    public void setAdminDesignation(String adminDesignation) { this.adminDesignation = adminDesignation; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public RegistrationStatus getStatus() { return status; }
    public void setStatus(RegistrationStatus status) { this.status = status; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public UUID getApprovedSchoolId() { return approvedSchoolId; }
    public void setApprovedSchoolId(UUID approvedSchoolId) { this.approvedSchoolId = approvedSchoolId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(Instant reviewedAt) { this.reviewedAt = reviewedAt; }
}
