package com.learnmate.backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record LearningResourceResponse(
        UUID id,
        UUID courseId,
        String title,
        String fileType,
        String uploadedByName,
        LocalDateTime uploadedAt
) {}