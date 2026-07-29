package com.learnmate.backend.dto;

import com.learnmate.backend.model.Role;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminUserResponse(
        UUID id,
        String email,
        String fullName,
        Role role,
        LocalDateTime createdAt
) {}