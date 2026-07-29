package com.learnmate.backend.repository;

import com.learnmate.backend.model.SkillsAssessment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SkillsAssessmentRepository extends JpaRepository<SkillsAssessment, UUID> {
    List<SkillsAssessment> findByStudentIdOrderBySubmittedAtDesc(UUID studentId);
}