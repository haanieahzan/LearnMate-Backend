package com.learnmate.backend.dto;

import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;

public record CourseAnalytics(
        UUID courseId,
        String courseCode,
        String courseTitle,
        int quizCount,
        int totalAttempts,
        BigDecimal classAverage,
        List<AtRiskStudent> atRiskStudents,
        List<AtRiskStudent> allStudents
) {}