package com.learnmate.backend.repository;

import com.learnmate.backend.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuizRepository extends JpaRepository<Quiz, UUID> {
    List<Quiz> findByCourseId(UUID courseId);
}