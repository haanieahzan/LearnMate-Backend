package com.learnmate.backend.dto;

import java.util.UUID;

public record FlashcardResponse(
        UUID id,
        String frontText,
        String backText
) {}