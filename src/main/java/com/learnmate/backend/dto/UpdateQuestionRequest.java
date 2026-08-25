package com.learnmate.backend.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record UpdateQuestionRequest(
        @NotBlank String questionText,
        List<String> options,
        @NotBlank String correctAnswer
) {}