package com.learnmate.backend.service;

import com.learnmate.backend.dto.RecommendationResponse;
import com.learnmate.backend.model.Quiz;
import com.learnmate.backend.model.QuizAttempt;
import com.learnmate.backend.model.User;
import com.learnmate.backend.repository.QuizAttemptRepository;
import com.learnmate.backend.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private static final BigDecimal WEAK_SKILL_THRESHOLD = BigDecimal.valueOf(60);
    private static final int MAX_UNTAKEN_SUGGESTIONS = 3;

    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizRepository quizRepository;

    @Transactional(readOnly = true)
    public List<RecommendationResponse> forStudent(User student) {
        List<QuizAttempt> attempts = quizAttemptRepository.findByStudentId(student.getId());
        List<RecommendationResponse> recommendations = new ArrayList<>();

        // Rule: never taken a quiz at all — a single, simple nudge, nothing
        // else to compute since there's no performance data yet.
        if (attempts.isEmpty()) {
            recommendations.add(new RecommendationResponse(
                    "get_started",
                    "Take your first quiz",
                    "You haven't attempted any quizzes yet. Try one from the Quiz Center to get started and see your skill breakdown.",
                    null, null, null
            ));
            return recommendations;
        }

        // Rule: weak skill areas — group attempts by resource (same "skill
        // area" concept as Skills Assessment), flag anything averaging below
        // the threshold, and recommend retaking the most recent quiz for it.
        Map<UUID, List<QuizAttempt>> byResource = attempts.stream()
                .collect(Collectors.groupingBy(a -> a.getQuiz().getResource().getId()));

        for (List<QuizAttempt> group : byResource.values()) {
            BigDecimal avg = group.stream()
                    .map(QuizAttempt::getScore)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(group.size()), 2, RoundingMode.HALF_UP);

            if (avg.compareTo(WEAK_SKILL_THRESHOLD) < 0) {
                QuizAttempt sample = group.get(group.size() - 1); // most recent attempt
                Quiz quiz = sample.getQuiz();
                String skillName = quiz.getSkillLabel() != null ? quiz.getSkillLabel() : quiz.getResource().getTitle();

                recommendations.add(new RecommendationResponse(
                        "weak_skill",
                        "Review: " + skillName,
                        "Your average score here is " + avg + "%. Retaking the quiz could help reinforce this material.",
                        quiz.getCourse().getId(),
                        quiz.getId(),
                        quiz.getResource().getId()
                ));
            }
        }

        // Rule: untaken quizzes — quizzes that exist but this student has
        // never attempted at all. Capped so the list stays useful, not overwhelming.
        Set<UUID> attemptedQuizIds = attempts.stream().map(a -> a.getQuiz().getId()).collect(Collectors.toSet());

        List<Quiz> untaken = quizRepository.findAll().stream()
                .filter(q -> !attemptedQuizIds.contains(q.getId()))
                .limit(MAX_UNTAKEN_SUGGESTIONS)
                .toList();

        for (Quiz quiz : untaken) {
            String skillName = quiz.getSkillLabel() != null ? quiz.getSkillLabel() : quiz.getResource().getTitle();
            recommendations.add(new RecommendationResponse(
                    "untaken_quiz",
                    "Try: " + quiz.getTitle(),
                    "You haven't attempted this quiz yet — a good way to test your understanding of " + skillName + ".",
                    quiz.getCourse().getId(),
                    quiz.getId(),
                    quiz.getResource().getId()
            ));
        }

        return recommendations;
    }
}