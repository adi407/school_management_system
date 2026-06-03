package com.sms.api.dto.module;

import com.sms.core.enums.StaffModule;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Used by the admin assignment page:
 *   GET /api/v1/staff/{id}/modules   → list of these per staff member
 *   GET /api/v1/modules/school       → all active assignments school-wide
 */
public record StaffModuleAssignmentDto(
    UUID         assignmentId,
    UUID         staffId,
    String       staffName,
    String       staffEmail,
    StaffModule  module,
    List<String> subPermissions,  // null → full access
    boolean      isActive,
    Instant      assignedAt
) {}
