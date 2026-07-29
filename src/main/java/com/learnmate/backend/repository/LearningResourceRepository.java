package com.learnmate.backend.repository;

import com.learnmate.backend.model.LearningResource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LearningResourceRepository extends JpaRepository<LearningResource, UUID> {
    List<LearningResource> findByCourseId(UUID courseId);
}