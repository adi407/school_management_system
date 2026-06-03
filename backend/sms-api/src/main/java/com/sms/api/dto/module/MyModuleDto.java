package com.sms.api.dto.module;

import com.sms.core.enums.StaffModule;

import java.util.List;

/**
 * Returned by GET /api/v1/my-modules — Angular caches this at login.
 * subPermissions null = full module access; non-null = restricted list.
 */
public record MyModuleDto(
    StaffModule  module,
    List<String> subPermissions   // null → full access
) {}
