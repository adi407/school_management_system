package com.sms.api.service;

import com.sms.api.dto.payroll.CreateExpenseRequest;
import com.sms.api.dto.payroll.ExpenseEntryDto;
import com.sms.api.entity.ExpenseEntry;
import com.sms.api.entity.User;
import com.sms.api.repository.ExpenseEntryRepository;
import com.sms.api.repository.UserRepository;
import com.sms.core.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ExpenseService {

    private final ExpenseEntryRepository expenseRepo;
    private final UserRepository         userRepo;

    public ExpenseService(ExpenseEntryRepository expenseRepo, UserRepository userRepo) {
        this.expenseRepo = expenseRepo;
        this.userRepo    = userRepo;
    }

    // ── Create ────────────────────────────────────────────────────────────────

    public ExpenseEntryDto create(UUID schoolId, UUID enteredByUserId, CreateExpenseRequest req) {
        User enteredBy = userRepo.findById(enteredByUserId)
            .orElseThrow(() -> new ResourceNotFoundException("User", enteredByUserId));

        ExpenseEntry e = new ExpenseEntry();
        e.setSchoolId(schoolId);
        e.setCategory(req.category());
        e.setDescription(req.description());
        e.setAmount(req.amount());
        e.setExpenseDate(req.expenseDate());
        e.setReferenceNo(req.referenceNo());
        e.setAttachmentUrl(req.attachmentUrl());
        e.setEnteredBy(enteredBy);

        return toDto(expenseRepo.save(e));
    }

    // ── Update ────────────────────────────────────────────────────────────────

    public ExpenseEntryDto update(UUID schoolId, UUID expenseId, CreateExpenseRequest req) {
        ExpenseEntry e = expenseRepo.findByIdAndSchoolId(expenseId, schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("ExpenseEntry", expenseId));

        e.setCategory(req.category());
        e.setDescription(req.description());
        e.setAmount(req.amount());
        e.setExpenseDate(req.expenseDate());
        e.setReferenceNo(req.referenceNo());
        if (req.attachmentUrl() != null) e.setAttachmentUrl(req.attachmentUrl());

        return toDto(expenseRepo.save(e));
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    public void delete(UUID schoolId, UUID expenseId) {
        ExpenseEntry e = expenseRepo.findByIdAndSchoolId(expenseId, schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("ExpenseEntry", expenseId));
        expenseRepo.delete(e);
    }

    // ── List in date range ────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ExpenseEntryDto> list(UUID schoolId, LocalDate from, LocalDate to) {
        return expenseRepo
            .findBySchoolIdAndExpenseDateBetweenOrderByExpenseDateDesc(schoolId, from, to)
            .stream().map(this::toDto).toList();
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private ExpenseEntryDto toDto(ExpenseEntry e) {
        User by = e.getEnteredBy();
        return new ExpenseEntryDto(
            e.getId(),
            e.getCategory(),
            e.getDescription(),
            e.getAmount(),
            e.getExpenseDate(),
            e.getReferenceNo(),
            e.getAttachmentUrl(),
            by != null ? by.getFullName() : null
        );
    }
}
