package com.learnmate.backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CourseResponse(
        UUID id,
        String code,
        String title,
        String lecturerName,
        String departmentName,
        LocalDateTime createdAt
) {}