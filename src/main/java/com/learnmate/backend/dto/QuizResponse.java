package com.learnmate.backend.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record QuizResponse(
        UUID id,
        UUID courseId,
        String title,
        LocalDateTime createdAt,
        List<QuizQuestionResponse> questions
) {}