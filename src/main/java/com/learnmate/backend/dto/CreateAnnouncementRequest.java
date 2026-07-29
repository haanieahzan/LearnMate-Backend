package com.learnmate.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateAnnouncementRequest(
        @NotBlank String title,
        @NotBlank String content
) {}