package com.sms.api.entity;

import com.sms.api.entity.base.SchoolScopedEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "announcements", indexes = {
    @Index(name = "idx_ann_school", columnList = "school_id")
})
public class Announcement extends SchoolScopedEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "target_roles", columnDefinition = "text[]")
    private List<String> targetRoles;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "published_by", nullable = false)
    private User publishedBy;

    @Column(nullable = false)
    private Instant publishedAt = Instant.now();

    private Instant expiresAt;

    @Column(nullable = false)
    private boolean isPinned = false;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public List<String> getTargetRoles() { return targetRoles; }
    public void setTargetRoles(List<String> targetRoles) { this.targetRoles = targetRoles; }
    public User getPublishedBy() { return publishedBy; }
    public void setPublishedBy(User publishedBy) { this.publishedBy = publishedBy; }
    public Instant getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public boolean isPinned() { return isPinned; }
    public void setPinned(boolean pinned) { isPinned = pinned; }
}
