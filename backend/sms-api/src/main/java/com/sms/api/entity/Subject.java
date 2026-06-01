package com.sms.api.entity;

import com.sms.api.entity.base.SchoolScopedEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "subjects",
    uniqueConstraints = @UniqueConstraint(columnNames = {"school_id", "code"}))
public class Subject extends SchoolScopedEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", insertable = false, updatable = false)
    private School school;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 20)
    private String code;

    /** CORE, ELECTIVE, ACTIVITY, LANGUAGE */
    @Column(nullable = false, length = 20)
    private String type = "CORE";

    private Integer creditHours;

    // ── Getters & Setters ─────────────────────────────────────
    public School getSchool() { return school; }
    public void setSchool(School school) { this.school = school; setSchoolId(school.getId()); }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Integer getCreditHours() { return creditHours; }
    public void setCreditHours(Integer creditHours) { this.creditHours = creditHours; }
}
