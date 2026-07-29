package com.learnmate.backend.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SkillsAssessmentResponse(
        UUID id,
        LocalDateTime submittedAt,
        List<AssessmentResultResponse> results
) {}