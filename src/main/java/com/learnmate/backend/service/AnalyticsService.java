package com.learnmate.backend.service;

import com.learnmate.backend.dto.*;
import com.learnmate.backend.model.QuizAttempt;
import com.learnmate.backend.model.User;
import com.learnmate.backend.repository.CourseRepository;
import com.learnmate.backend.repository.QuizAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.learnmate.backend.model.Course;
import com.learnmate.backend.model.Quiz;
import com.learnmate.backend.repository.QuizRepository;
import java.util.*;
import java.util.stream.Collectors;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private static final BigDecimal WEAK_SKILL_THRESHOLD = BigDecimal.valueOf(60);

    private final QuizAttemptRepository quizAttemptRepository;
    private final CourseRepository courseRepository;
    private final QuizRepository quizRepository;
    private final StreakService streakService;
    private final SkillsAssessmentService skillsAssessmentService;

    @Transactional(readOnly = true)
    public StudentAnalyticsResponse forStudent(User student) {
        List<QuizAttempt> attempts = quizAttemptRepository.findByStudentId(student.getId());

        BigDecimal averageScore = attempts.isEmpty()
                ? BigDecimal.ZERO
                : attempts.stream()
                .map(QuizAttempt::getScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(attempts.size()), 2, RoundingMode.HALF_UP);

        // Reuses the exact same Field->Skill computation the Skills page shows,
        // so this number always agrees with what the student sees there.
        List<FieldScore> fields = skillsAssessmentService.currentSkillsByField(student);
        int weakSkillCount = (int) fields.stream()
                .flatMap(f -> f.skills().stream())
                .filter(s -> s.averageScore().compareTo(WEAK_SKILL_THRESHOLD) < 0)
                .count();

        List<RecentAttemptSummary> recent = attempts.stream()
                .sorted(Comparator.comparing(QuizAttempt::getAttemptedAt).reversed())
                .limit(5)
                .map(a -> new RecentAttemptSummary(a.getQuiz().getTitle(), a.getScore(), a.getAttemptedAt()))
                .toList();

        StreakResponse streak = streakService.forStudent(student);

        return new StudentAnalyticsResponse(
                courseRepository.count(),
                attempts.size(),
                averageScore,
                weakSkillCount,
                streak.currentStreak(),
                recent
        );
    }

    @Transactional(readOnly = true)
    public CourseProgressResponse forStudentCourse(User student, UUID courseId) {
        List<QuizAttempt> attempts = quizAttemptRepository.findByStudentId(student.getId()).stream()
                .filter(a -> a.getQuiz().getCourse().getId().equals(courseId))
                .toList();

        if (attempts.isEmpty()) {
            return new CourseProgressResponse(courseId, null, null, 0, BigDecimal.ZERO, List.of(), List.of());
        }

        var course = attempts.get(0).getQuiz().getCourse();

        BigDecimal averageScore = average(attempts);

        // Group by resource-derived skill area, same pattern as SkillsAssessmentService,
        // but naturally scoped to just this one course's attempts.
        Map<UUID, List<QuizAttempt>> byResource = attempts.stream()
                .collect(Collectors.groupingBy(a -> a.getQuiz().getResource().getId()));

        List<SkillAreaScore> skills = byResource.values().stream()
                .map(group -> {
                    QuizAttempt sample = group.stream()
                            .filter(a -> a.getQuiz().getSkillLabel() != null && !a.getQuiz().getSkillLabel().isBlank())
                            .findFirst()
                            .orElse(group.get(0));
                    String skillArea = sample.getQuiz().getSkillLabel() != null
                            ? sample.getQuiz().getSkillLabel() : sample.getQuiz().getResource().getTitle();
                    return new SkillAreaScore(skillArea, course.getCode(), average(group), group.size(), sample.getQuiz().getResource().getId());
                })
                .sorted(Comparator.comparing(SkillAreaScore::skillArea))
                .toList();

        List<RecentAttemptSummary> recent = attempts.stream()
                .sorted(Comparator.comparing(QuizAttempt::getAttemptedAt).reversed())
                .limit(5)
                .map(a -> new RecentAttemptSummary(a.getQuiz().getTitle(), a.getScore(), a.getAttemptedAt()))
                .toList();

        return new CourseProgressResponse(
                courseId, course.getCode(), course.getTitle(),
                attempts.size(), averageScore, skills, recent
        );
    }
    private BigDecimal average(List<QuizAttempt> attempts) {
        return attempts.stream()
                .map(QuizAttempt::getScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(attempts.size()), 2, RoundingMode.HALF_UP);
    }


    @Transactional(readOnly = true)
    public LecturerAnalyticsResponse forLecturer(User lecturer) {
        List<Course> courses = courseRepository.findByLecturerId(lecturer.getId());
        Set<UUID> uniqueStudents = new HashSet<>();

        List<CourseAnalytics> courseAnalytics = courses.stream().map(course -> {
            List<Quiz> quizzes = quizRepository.findByCourseId(course.getId());
            List<UUID> quizIds = quizzes.stream().map(Quiz::getId).toList();

            List<QuizAttempt> attempts = quizIds.isEmpty()
                    ? List.of()
                    : quizAttemptRepository.findByQuizIdIn(quizIds);

            attempts.forEach(a -> uniqueStudents.add(a.getStudent().getId()));

            BigDecimal classAverage = attempts.isEmpty()
                    ? BigDecimal.ZERO
                    : attempts.stream()
                    .map(QuizAttempt::getScore)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(attempts.size()), 2, RoundingMode.HALF_UP);

            // At-risk: group each course's attempts by student, average per
            // student, flag anyone below the same 60% threshold used everywhere
            // else in the app (Recommendations, Skills) for consistency.
            Map<UUID, List<QuizAttempt>> byStudent = attempts.stream()
                    .collect(Collectors.groupingBy(a -> a.getStudent().getId()));

            List<AtRiskStudent> everyStudent = byStudent.values().stream()
                    .map(group -> {
                        BigDecimal avg = group.stream()
                                .map(QuizAttempt::getScore)
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                                .divide(BigDecimal.valueOf(group.size()), 2, RoundingMode.HALF_UP);
                        User student = group.get(0).getStudent();
                        return new AtRiskStudent(student.getFullName(), student.getEmail(), avg);
                    })
                    .sorted(Comparator.comparing(AtRiskStudent::fullName))
                    .toList();

            List<AtRiskStudent> atRisk = everyStudent.stream()
                    .filter(s -> s.averageScore().compareTo(WEAK_SKILL_THRESHOLD) < 0)
                    .sorted(Comparator.comparing(AtRiskStudent::averageScore))
                    .toList();

            return new CourseAnalytics(
                    course.getId(), course.getCode(), course.getTitle(),
                    quizzes.size(), attempts.size(), classAverage, atRisk, everyStudent
            );
        }).toList();

        return new LecturerAnalyticsResponse(courses.size(), uniqueStudents.size(), courseAnalytics);
    }

}