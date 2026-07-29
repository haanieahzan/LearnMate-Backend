package com.learnmate.backend.dto;

import com.learnmate.backend.model.Role;

import java.util.UUID;

public record AuthResponse(
        String token,
        UUID userId,
        String email,
        String fullName,
        Role role
) {}
