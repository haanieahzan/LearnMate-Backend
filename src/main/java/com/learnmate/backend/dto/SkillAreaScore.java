package com.learnmate.backend.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record SkillAreaScore(
        String skillArea,      // the resource title this skill is derived from
        String courseCode,
        BigDecimal averageScore,
        int attemptCount,
        UUID resourceId

){}