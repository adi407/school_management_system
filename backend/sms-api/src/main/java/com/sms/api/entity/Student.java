package com.sms.api.entity;

import com.sms.api.entity.base.SchoolScopedEntity;
import com.sms.core.enums.Gender;
import com.sms.core.enums.StudentCategory;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;

@Entity
@Table(name = "students", indexes = {
    @Index(name = "idx_student_school", columnList = "school_id"),
    @Index(name = "idx_student_admission", columnList = "admission_no"),
    @Index(name = "idx_student_class", columnList = "class_id")
})
public class Student extends SchoolScopedEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", insertable = false, updatable = false)
    private School school;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(name = "admission_no", nullable = false)
    private String admissionNo;

    private String rollNo;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    private String bloodGroup;
    private String nationality = "Indian";
    private String religion;
    private String caste;

    @Enumerated(EnumType.STRING)
    private StudentCategory category = StudentCategory.GEN;

    private String motherTongue;
    private String aadhaarNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private SchoolClass schoolClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id")
    private AcademicYear academicYear;

    private String houseGroup;

    @Column(nullable = false)
    private boolean isActive = true;

    @Column(nullable = false)
    private LocalDate admissionDate;

    private boolean tcIssued = false;
    private String photoUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String medicalConditions;

    public School getSchool() { return school; }
    public void setSchool(School school) { this.school = school; setSchoolId(school.getId()); }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getAdmissionNo() { return admissionNo; }
    public void setAdmissionNo(String admissionNo) { this.admissionNo = admissionNo; }
    public String getRollNo() { return rollNo; }
    public void setRollNo(String rollNo) { this.rollNo = rollNo; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getFullName() { return firstName + " " + lastName; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }
    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }
    public String getReligion() { return religion; }
    public void setReligion(String religion) { this.religion = religion; }
    public String getCaste() { return caste; }
    public void setCaste(String caste) { this.caste = caste; }
    public StudentCategory getCategory() { return category; }
    public void setCategory(StudentCategory category) { this.category = category; }
    public String getMotherTongue() { return motherTongue; }
    public void setMotherTongue(String motherTongue) { this.motherTongue = motherTongue; }
    public String getAadhaarNo() { return aadhaarNo; }
    public void setAadhaarNo(String aadhaarNo) { this.aadhaarNo = aadhaarNo; }
    public SchoolClass getSchoolClass() { return schoolClass; }
    public void setSchoolClass(SchoolClass schoolClass) { this.schoolClass = schoolClass; }
    public AcademicYear getAcademicYear() { return academicYear; }
    public void setAcademicYear(AcademicYear academicYear) { this.academicYear = academicYear; }
    public String getHouseGroup() { return houseGroup; }
    public void setHouseGroup(String houseGroup) { this.houseGroup = houseGroup; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public LocalDate getAdmissionDate() { return admissionDate; }
    public void setAdmissionDate(LocalDate admissionDate) { this.admissionDate = admissionDate; }
    public boolean isTcIssued() { return tcIssued; }
    public void setTcIssued(boolean tcIssued) { this.tcIssued = tcIssued; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public String getMedicalConditions() { return medicalConditions; }
    public void setMedicalConditions(String medicalConditions) { this.medicalConditions = medicalConditions; }
}
