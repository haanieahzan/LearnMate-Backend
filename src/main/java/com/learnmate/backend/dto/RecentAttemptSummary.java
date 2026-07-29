package com.learnmate.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RecentAttemptSummary(
        String quizTitle,
        BigDecimal score,
        LocalDateTime attemptedAt
) {}