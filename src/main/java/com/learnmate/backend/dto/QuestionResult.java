package com.learnmate.backend.dto;

import java.util.UUID;

public record QuestionResult(
        UUID questionId,
        String questionText,
        String selectedAnswer,
        String correctAnswer,
        boolean correct
) {}