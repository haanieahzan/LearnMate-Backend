package com.learnmate.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "assessment_results")
@Getter
@Setter
@NoArgsConstructor
public class AssessmentResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    private SkillsAssessment assessment;

    @Column(name = "skill_area", nullable = false, length = 100)
    private String skillArea;

    @Column(name = "confidence_score", nullable = false)
    private BigDecimal confidenceScore;
}