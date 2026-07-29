package com.learnmate.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateFieldRequest(
        @NotBlank String name
) {}