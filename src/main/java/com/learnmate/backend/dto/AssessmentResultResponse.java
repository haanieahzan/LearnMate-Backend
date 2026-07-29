package com.learnmate.backend.dto;

import java.math.BigDecimal;

public record AssessmentResultResponse(
        String skillArea,
        BigDecimal confidenceScore
) {}