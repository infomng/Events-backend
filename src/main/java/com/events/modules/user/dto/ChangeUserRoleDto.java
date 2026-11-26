package com.events.modules.user.dto;

import com.events.modules.user.enumeration.RoleEnum;
import jakarta.validation.constraints.NotNull;

public record ChangeUserRoleDto(
        @NotNull(message = "Role is required")
        RoleEnum role
) {
}
