package com.sms.api.entity;

import com.sms.api.entity.base.SchoolScopedEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "fee_payments", indexes = {
    @Index(name = "idx_fee_pay_student",   columnList = "student_id"),
    @Index(name = "idx_fee_pay_school",    columnList = "school_id"),
    @Index(name = "idx_fee_pay_receipt",   columnList = "receipt_no", unique = true)
})
public class FeePayment extends SchoolScopedEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fee_structure_id")
    private FeeStructure feeStructure;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collected_by", nullable = false)
    private User collectedBy;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amountPaid;

    @Column(nullable = false)
    private LocalDate paymentDate;

    /** CASH | ONLINE | CHEQUE | DD | UPI */
    @Column(nullable = false, length = 20)
    private String paymentMode;

    @Column(name = "receipt_no", nullable = false, unique = true, length = 30)
    private String receiptNo;

    @Column(length = 500)
    private String remarks;

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public FeeStructure getFeeStructure() { return feeStructure; }
    public void setFeeStructure(FeeStructure feeStructure) { this.feeStructure = feeStructure; }
    public User getCollectedBy() { return collectedBy; }
    public void setCollectedBy(User collectedBy) { this.collectedBy = collectedBy; }
    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }
    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }
    public String getReceiptNo() { return receiptNo; }
    public void setReceiptNo(String receiptNo) { this.receiptNo = receiptNo; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
