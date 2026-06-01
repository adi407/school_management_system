package com.sms.api.entity;

import com.sms.core.enums.BoardType;
import com.sms.core.enums.SubscriptionTier;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "schools", indexes = {
    @Index(name = "idx_schools_code", columnList = "code", unique = true),
    @Index(name = "idx_schools_platform", columnList = "platform_id")
})
@EntityListeners(AuditingEntityListener.class)
public class School {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "platform_id", nullable = false)
    private Platform platform;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    private String logoUrl;
    private String address;
    private String phone;
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BoardType board = BoardType.CBSE;

    @Column(nullable = false, length = 50)
    private String timezone = "Asia/Kolkata";

    @Column(nullable = false, length = 10)
    private String locale = "en-IN";

    @Column(nullable = false)
    private boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionTier subscriptionTier = SubscriptionTier.FREE;

    private LocalDate subscriptionExpiry;

    private Integer maxStudents;
    private Integer maxStaff;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    // Getters and setters
    public UUID getId() { return id; }
    public Platform getPlatform() { return platform; }
    public void setPlatform(Platform platform) { this.platform = platform; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public BoardType getBoard() { return board; }
    public void setBoard(BoardType board) { this.board = board; }
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    public String getLocale() { return locale; }
    public void setLocale(String locale) { this.locale = locale; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public SubscriptionTier getSubscriptionTier() { return subscriptionTier; }
    public void setSubscriptionTier(SubscriptionTier subscriptionTier) { this.subscriptionTier = subscriptionTier; }
    public LocalDate getSubscriptionExpiry() { return subscriptionExpiry; }
    public void setSubscriptionExpiry(LocalDate subscriptionExpiry) { this.subscriptionExpiry = subscriptionExpiry; }
    public Integer getMaxStudents() { return maxStudents; }
    public void setMaxStudents(Integer maxStudents) { this.maxStudents = maxStudents; }
    public Integer getMaxStaff() { return maxStaff; }
    public void setMaxStaff(Integer maxStaff) { this.maxStaff = maxStaff; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
