package com.learnmate.backend.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record GenerateFlashcardsRequest(
        @NotNull UUID resourceId,
        int numCards,
        String provider,
        String ollamaModel
) {}