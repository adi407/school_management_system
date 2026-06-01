package com.sms.api.entity;

import com.sms.api.entity.base.SchoolScopedEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "fee_structures", indexes = {
    @Index(name = "idx_fee_struct_school", columnList = "school_id"),
    @Index(name = "idx_fee_struct_class",  columnList = "class_id")
})
public class FeeStructure extends SchoolScopedEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")      // null = applies to all classes
    private SchoolClass schoolClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id")
    private AcademicYear academicYear;

    @Column(nullable = false, length = 100)
    private String feeType;             // e.g. "Tuition", "Transport", "Library"

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    private LocalDate dueDate;

    @Column(nullable = false)
    private boolean isRecurring = false; // monthly recurring vs one-time

    @Column(length = 10)
    private String frequency;            // MONTHLY | QUARTERLY | ANNUAL | ONE_TIME

    @Column(length = 500)
    private String description;

    public SchoolClass getSchoolClass() { return schoolClass; }
    public void setSchoolClass(SchoolClass schoolClass) { this.schoolClass = schoolClass; }
    public AcademicYear getAcademicYear() { return academicYear; }
    public void setAcademicYear(AcademicYear academicYear) { this.academicYear = academicYear; }
    public String getFeeType() { return feeType; }
    public void setFeeType(String feeType) { this.feeType = feeType; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public boolean isRecurring() { return isRecurring; }
    public void setRecurring(boolean recurring) { isRecurring = recurring; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
