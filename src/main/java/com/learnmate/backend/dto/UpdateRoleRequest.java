package com.learnmate.backend.dto;

import com.learnmate.backend.model.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateRoleRequest(
        @NotNull Role role
) {}