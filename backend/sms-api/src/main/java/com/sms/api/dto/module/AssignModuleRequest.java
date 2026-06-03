package com.sms.api.dto.module;

import com.sms.core.enums.StaffModule;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AssignModuleRequest(
    @NotNull StaffModule module,

    /**
     * null or empty = full module access.
     * Populated = restrict to only these ModulePermission values.
     */
    List<String> subPermissions
) {}
