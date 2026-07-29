package com.learnmate.backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AnnouncementResponse(
        UUID id,
        UUID courseId,
        String title,
        String content,
        String postedByName,
        LocalDateTime createdAt
) {}