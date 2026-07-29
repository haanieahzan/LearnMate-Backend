package com.learnmate.backend.repository;

import com.learnmate.backend.model.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, UUID> {
    List<QuizAttempt> findByStudentId(UUID studentId);
    List<QuizAttempt> findByQuizId(UUID quizId);
    List<QuizAttempt> findByQuizIdIn(List<UUID> quizIds);
}