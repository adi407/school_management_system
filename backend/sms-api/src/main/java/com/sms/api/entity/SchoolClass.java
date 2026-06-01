package com.sms.api.entity;

import com.sms.api.entity.base.SchoolScopedEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "school_classes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"school_id", "grade", "section"})
})
public class SchoolClass extends SchoolScopedEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", insertable = false, updatable = false)
    private School school;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int grade;

    @Column(nullable = false, length = 5)
    private String section;

    private int capacity = 40;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_teacher_id")
    private User classTeacher;

    public School getSchool() { return school; }
    public void setSchool(School school) { this.school = school; setSchoolId(school.getId()); }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getGrade() { return grade; }
    public void setGrade(int grade) { this.grade = grade; }
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public User getClassTeacher() { return classTeacher; }
    public void setClassTeacher(User classTeacher) { this.classTeacher = classTeacher; }
}
