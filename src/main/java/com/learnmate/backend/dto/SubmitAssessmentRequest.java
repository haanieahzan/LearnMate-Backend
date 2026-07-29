package com.learnmate.backend.dto;

import jakarta.validation.constraints.NotEmpty;

import java.math.BigDecimal;
import java.util.Map;

public record SubmitAssessmentRequest(
        // Maps skillArea -> confidence score (e.g. "Java OOP" -> 7, on a 1-10 scale)
        @NotEmpty Map<String, BigDecimal> ratings
) {}