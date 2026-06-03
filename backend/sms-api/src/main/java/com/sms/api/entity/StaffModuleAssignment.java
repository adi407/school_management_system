package com.sms.api.entity;

import com.sms.api.entity.base.SchoolScopedEntity;
import com.sms.core.enums.StaffModule;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;

/**
 * Records which modules a staff member has been granted access to.
 *
 * sub_permissions = null          → full access to every action in the module
 * sub_permissions = ["X","Y"]    → only those specific ModulePermission values
 *
 * One row per (school, user, module). Unique constraint prevents duplicates.
 * To update sub-permissions, update the existing row.
 */
@Entity
@Table(name = "staff_module_assignments", indexes = {
    @Index(name = "idx_sma_school",    columnList = "school_id"),
    @Index(name = "idx_sma_user",      columnList = "user_id"),
    @Index(name = "idx_sma_school_user", columnList = "school_id, user_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uq_sma_user_module",
        columnNames = {"school_id", "user_id", "module"})
})
public class StaffModuleAssignment extends SchoolScopedEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User staff;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StaffModule module;

    /**
     * Null = full module access.
     * Non-null = restricted to the listed ModulePermission string values.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "sub_permissions", columnDefinition = "jsonb")
    private List<String> subPermissions;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by")
    private User assignedBy;

    @Column(name = "assigned_at")
    private Instant assignedAt = Instant.now();

    @Column(nullable = false)
    private boolean isActive = true;

    // ── Getters / Setters ──────────────────────────────────────────────────────

    public User getStaff() { return staff; }
    public void setStaff(User staff) { this.staff = staff; }

    public StaffModule getModule() { return module; }
    public void setModule(StaffModule module) { this.module = module; }

    public List<String> getSubPermissions() { return subPermissions; }
    public void setSubPermissions(List<String> subPermissions) { this.subPermissions = subPermissions; }

    public User getAssignedBy() { return assignedBy; }
    public void setAssignedBy(User assignedBy) { this.assignedBy = assignedBy; }

    public Instant getAssignedAt() { return assignedAt; }
    public void setAssignedAt(Instant assignedAt) { this.assignedAt = assignedAt; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    /** Convenience: does this assignment grant a specific permission? */
    public boolean grantsPermission(String permission) {
        if (!isActive) return false;
        if (subPermissions == null || subPermissions.isEmpty()) return true; // full access
        return subPermissions.contains(permission);
    }
}
