package com.learnmate.backend.dto;

import java.math.BigDecimal;

public record AtRiskStudent(
        String fullName,
        String email,
        BigDecimal averageScore
) {}