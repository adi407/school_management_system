package com.sms.api.entity;

import com.sms.core.enums.FeatureKey;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "feature_flags",
    uniqueConstraints = @UniqueConstraint(columnNames = {"school_id", "feature_key"}),
    indexes = @Index(name = "idx_ff_school", columnList = "school_id"))
@EntityListeners(AuditingEntityListener.class)
public class FeatureFlag {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @Enumerated(EnumType.STRING)
    @Column(name = "feature_key", nullable = false)
    private FeatureKey featureKey;

    @Column(nullable = false)
    private boolean isEnabled = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String config;

    private UUID enabledBy;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public UUID getId() { return id; }
    public School getSchool() { return school; }
    public void setSchool(School school) { this.school = school; }
    public FeatureKey getFeatureKey() { return featureKey; }
    public void setFeatureKey(FeatureKey featureKey) { this.featureKey = featureKey; }
    public boolean isEnabled() { return isEnabled; }
    public void setEnabled(boolean enabled) { isEnabled = enabled; }
    public String getConfig() { return config; }
    public void setConfig(String config) { this.config = config; }
    public UUID getEnabledBy() { return enabledBy; }
    public void setEnabledBy(UUID enabledBy) { this.enabledBy = enabledBy; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
