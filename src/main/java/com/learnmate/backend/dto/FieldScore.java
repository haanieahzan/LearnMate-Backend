package com.learnmate.backend.dto;

import java.math.BigDecimal;
import java.util.List;

public record FieldScore(
        String fieldName,
        BigDecimal averageScore,
        List<SkillAreaScore> skills
) {}