package com.learnmate.backend.dto;

import java.util.List;
import java.util.UUID;

public record QuizQuestionReviewResponse(
        UUID id,
        String questionText,
        String questionType,
        List<String> options,
        String correctAnswer
) {}