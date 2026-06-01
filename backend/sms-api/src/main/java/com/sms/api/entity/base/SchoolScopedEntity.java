package com.sms.api.entity.base;

import jakarta.persistence.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * Base class for all school-scoped entities.
 * The Hibernate @Filter ensures every JPQL query automatically appends
 * "AND school_id = :schoolId", preventing cross-tenant data leaks.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@FilterDef(name = "schoolFilter", parameters = @ParamDef(name = "schoolId", type = UUID.class))
@Filter(name = "schoolFilter", condition = "school_id = :schoolId")
public abstract class SchoolScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "school_id", nullable = false, updatable = false)
    private UUID schoolId;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public UUID getId() { return id; }
    public UUID getSchoolId() { return schoolId; }
    public void setSchoolId(UUID schoolId) { this.schoolId = schoolId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
