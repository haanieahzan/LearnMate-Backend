package com.learnmate.backend.repository;

import com.learnmate.backend.model.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, UUID> {
    List<QuizQuestion> findByQuizId(UUID quizId);
    List<QuizQuestion> findByQuizIdIn(List<UUID> quizIds);
}