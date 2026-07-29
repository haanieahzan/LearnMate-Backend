package com.learnmate.backend.dto;

import java.util.UUID;

public record RecommendationResponse(
        String type,        // "weak_skill" | "untaken_quiz" | "get_started"
        String title,
        String description,
        UUID courseId,
        UUID quizId
) {}