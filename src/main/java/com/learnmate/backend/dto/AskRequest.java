package com.learnmate.backend.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record AskRequest(
        @NotBlank String question,
        UUID resourceId,   // optional — null means search across all documents
        String provider,   // optional — "gemini" or "ollama", overrides the admin default for this request only
        String ollamaModel // optional — which local model to use, if provider is "ollama"
) {}