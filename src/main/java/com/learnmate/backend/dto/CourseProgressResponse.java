package com.learnmate.backend.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CourseProgressResponse(
        UUID courseId,
        String courseCode,
        String courseTitle,
        int quizzesTaken,
        BigDecimal averageScore,
        List<SkillAreaScore> skills,
        List<RecentAttemptSummary> recentAttempts
) {}