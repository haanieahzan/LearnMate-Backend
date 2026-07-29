package com.learnmate.backend.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record GenerateQuizRequest(
        @NotNull UUID resourceId,
        int numQuestions,
        String difficulty,
        String provider,     // optional per-request override
        String ollamaModel   // optional per-request override
) {}