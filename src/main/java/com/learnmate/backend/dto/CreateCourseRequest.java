package com.learnmate.backend.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CreateCourseRequest(
        @NotBlank String code,
        @NotBlank String title,
        UUID departmentId,
        UUID fieldId
) {}