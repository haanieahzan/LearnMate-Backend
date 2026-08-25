package com.learnmate.backend.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record QuizResponse(
        UUID id,
        UUID courseId,
        String courseCode,
        String title,
        String skillLabel,
        String difficulty,
        String questionFormat,
        boolean published,
        LocalDateTime createdAt,
        List<QuizQuestionResponse> questions
) {}