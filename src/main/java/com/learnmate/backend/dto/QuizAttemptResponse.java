package com.learnmate.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record QuizAttemptResponse(
        UUID id,
        UUID quizId,
        BigDecimal score,
        int correctCount,
        int totalQuestions,
        LocalDateTime attemptedAt,
        List<QuestionResult> breakdown
) {}