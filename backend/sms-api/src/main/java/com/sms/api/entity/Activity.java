package com.sms.api.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "activities", indexes = { @Index(name = "idx_activity_school", columnList = "school_id") })
public class Activity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 50)
    private String category; // SPORTS | ACADEMIC | MUSIC | ARTS | CULTURAL

    @Column(length = 200)
    private String coach;

    @Column(length = 200)
    private String schedule;

    @Column(nullable = false)
    private int capacity = 30;

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(updatable = false)
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    @PreUpdate void touch() { updatedAt = Instant.now(); }

    // ── Getters & Setters ─────────────────────────────────────────────────────
    public UUID getId()               { return id; }
    public UUID getSchoolId()         { return schoolId; }
    public void setSchoolId(UUID s)   { schoolId = s; }
    public String getName()           { return name; }
    public void setName(String n)     { name = n; }
    public String getCategory()       { return category; }
    public void setCategory(String c) { category = c; }
    public String getCoach()          { return coach; }
    public void setCoach(String c)    { coach = c; }
    public String getSchedule()       { return schedule; }
    public void setSchedule(String s) { schedule = s; }
    public int getCapacity()          { return capacity; }
    public void setCapacity(int n)    { capacity = n; }
    public String getStatus()         { return status; }
    public void setStatus(String s)   { status = s; }
    public Instant getCreatedAt()     { return createdAt; }
    public Instant getUpdatedAt()     { return updatedAt; }
}
