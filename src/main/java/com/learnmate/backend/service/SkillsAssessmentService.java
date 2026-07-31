package com.learnmate.backend.service;

import com.learnmate.backend.dto.FieldScore;
import com.learnmate.backend.dto.QuizAttemptSummary;
import com.learnmate.backend.dto.SkillAreaScore;
import com.learnmate.backend.model.QuizAttempt;
import com.learnmate.backend.model.User;
import com.learnmate.backend.repository.QuizAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkillsAssessmentService {

    private final QuizAttemptRepository quizAttemptRepository;

    @Transactional(readOnly = true)
    public List<FieldScore> currentSkillsByField(User student) {
        List<QuizAttempt> attempts = quizAttemptRepository.findByStudentId(student.getId());

        // Level 1: group by field name (falling back to "Unassigned" if a
        // course has no field set — old data, or a lecturer skipped it).
        Map<String, List<QuizAttempt>> byField = attempts.stream()
                .collect(Collectors.groupingBy(a -> {
                    var field = a.getQuiz().getCourse().getField();
                    return field != null ? field.getName() : "Unassigned";
                }));

        return byField.entrySet().stream()
                .map(fieldEntry -> {
                    String fieldName = fieldEntry.getKey();
                    List<QuizAttempt> fieldAttempts = fieldEntry.getValue();

                    // Level 2: within this field, group by skill area (resource-level,
                    // same logic as before, just nested one level deeper now).
                    Map<String, List<QuizAttempt>> bySkill = fieldAttempts.stream()
                            .collect(Collectors.groupingBy(a -> a.getQuiz().getResource().getId().toString()));

                    List<SkillAreaScore> skills = bySkill.values().stream()
                            .map(group -> {
                                QuizAttempt sample = group.stream()
                                        .filter(a -> a.getQuiz().getSkillLabel() != null && !a.getQuiz().getSkillLabel().isBlank())
                                        .findFirst()
                                        .orElse(group.get(0));
                                BigDecimal avg = average(group);
                                String skillArea = sample.getQuiz().getSkillLabel() != null
                                        ? sample.getQuiz().getSkillLabel() : sample.getQuiz().getResource().getTitle();
                                return new SkillAreaScore(skillArea, sample.getQuiz().getCourse().getCode(), avg, group.size(), sample.getQuiz().getResource().getId());
                            })
                            .sorted(Comparator.comparing(SkillAreaScore::skillArea))
                            .toList();

                    BigDecimal fieldAvg = average(fieldAttempts);
                    return new FieldScore(fieldName, fieldAvg, skills);
                })
                .sorted(Comparator.comparing(FieldScore::fieldName))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<QuizAttemptSummary> history(User student) {
        return quizAttemptRepository.findByStudentId(student.getId()).stream()
                .sorted(Comparator.comparing(QuizAttempt::getAttemptedAt).reversed())
                .map(a -> new QuizAttemptSummary(
                        a.getQuiz().getSkillLabel() != null ? a.getQuiz().getSkillLabel() : a.getQuiz().getResource().getTitle(),
                        a.getQuiz().getTitle(),
                        a.getScore(),
                        a.getAttemptedAt()
                ))
                .toList();
    }

    private BigDecimal average(List<QuizAttempt> attempts) {
        return attempts.stream()
                .map(QuizAttempt::getScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(attempts.size()), 2, RoundingMode.HALF_UP);
    }
}