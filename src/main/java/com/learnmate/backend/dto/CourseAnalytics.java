package com.learnmate.backend.dto;

import java.util.UUID;
import java.math.BigDecimal;

public record CourseAnalytics(
        UUID courseId,
        String courseCode,
        String courseTitle,
        int quizCount,
        int totalAttempts,
        BigDecimal classAverage,
        java.util.List<AtRiskStudent> atRiskStudents
) {}