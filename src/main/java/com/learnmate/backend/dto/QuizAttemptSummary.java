package com.learnmate.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record QuizAttemptSummary(
        String skillArea,
        String quizTitle,
        BigDecimal score,
        LocalDateTime attemptedAt
) {}