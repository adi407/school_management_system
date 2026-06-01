package com.sms.api.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "guardians", indexes = {
    @Index(name = "idx_guardian_student", columnList = "student_id")
})
public class Guardian {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 30)
    private String relation;

    @Column(nullable = false, length = 20)
    private String phone;

    private String email;
    private String aadhaarNo;
    private String occupation;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(nullable = false)
    private boolean isPrimary = false;

    @Column(nullable = false)
    private boolean isAuthorizedPickup = false;

    private String photoUrl;

    @Column(updatable = false)
    private Instant createdAt = Instant.now();

    // ── Getters & Setters ─────────────────────────────────
    public UUID getId() { return id; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public UUID getSchoolId() { return schoolId; }
    public void setSchoolId(UUID schoolId) { this.schoolId = schoolId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRelation() { return relation; }
    public void setRelation(String relation) { this.relation = relation; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAadhaarNo() { return aadhaarNo; }
    public void setAadhaarNo(String aadhaarNo) { this.aadhaarNo = aadhaarNo; }
    public String getOccupation() { return occupation; }
    public void setOccupation(String occupation) { this.occupation = occupation; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public boolean isPrimary() { return isPrimary; }
    public void setPrimary(boolean primary) { isPrimary = primary; }
    public boolean isAuthorizedPickup() { return isAuthorizedPickup; }
    public void setAuthorizedPickup(boolean authorizedPickup) { isAuthorizedPickup = authorizedPickup; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public Instant getCreatedAt() { return createdAt; }
}
