package com.learnmate.backend.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.Map;
import java.util.UUID;

public record SubmitQuizAttemptRequest(
        // Maps questionId -> the option text the student picked
        @NotEmpty Map<UUID, String> answers
) {}