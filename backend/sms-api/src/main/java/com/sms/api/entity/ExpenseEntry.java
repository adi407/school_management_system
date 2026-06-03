package com.sms.api.entity;

import com.sms.api.entity.base.SchoolScopedEntity;
import com.sms.core.enums.ExpenseCategory;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Non-payroll operational expense entered manually by the accountant.
 * Used to build the school's P&L report (Rent, Electricity, Maintenance, etc.)
 */
@Entity
@Table(name = "expense_entries", indexes = {
    @Index(name = "idx_expense_school",    columnList = "school_id"),
    @Index(name = "idx_expense_date",      columnList = "school_id, expense_date"),
    @Index(name = "idx_expense_category",  columnList = "school_id, category")
})
public class ExpenseEntry extends SchoolScopedEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ExpenseCategory category;

    @Column(nullable = false, length = 200)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    /** Optional invoice / receipt reference */
    @Column(length = 100)
    private String referenceNo;

    /** Optional attachment URL (S3 key for receipts) */
    @Column(length = 500)
    private String attachmentUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entered_by")
    private User enteredBy;

    // ── Getters / Setters ──────────────────────────────────────────────────────

    public ExpenseCategory getCategory() { return category; }
    public void setCategory(ExpenseCategory category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDate getExpenseDate() { return expenseDate; }
    public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }

    public String getReferenceNo() { return referenceNo; }
    public void setReferenceNo(String referenceNo) { this.referenceNo = referenceNo; }

    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }

    public User getEnteredBy() { return enteredBy; }
    public void setEnteredBy(User enteredBy) { this.enteredBy = enteredBy; }
}
