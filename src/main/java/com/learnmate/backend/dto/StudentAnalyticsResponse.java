package com.learnmate.backend.dto;

import java.math.BigDecimal;
import java.util.List;

public record StudentAnalyticsResponse(
        long totalCourses,
        int quizzesTaken,
        BigDecimal averageScore,
        int weakSkillCount,
        int currentStreak,
        List<RecentAttemptSummary> recentAttempts
) {}