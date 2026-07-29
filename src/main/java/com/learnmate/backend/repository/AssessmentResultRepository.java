package com.learnmate.backend.repository;

import com.learnmate.backend.model.AssessmentResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AssessmentResultRepository extends JpaRepository<AssessmentResult, UUID> {
}