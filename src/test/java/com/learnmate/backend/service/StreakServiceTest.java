package com.learnmate.backend.service;

import com.learnmate.backend.model.Quiz;
import com.learnmate.backend.model.QuizAttempt;
import com.learnmate.backend.model.User;
import com.learnmate.backend.repository.QuizAttemptRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests the streak calculation logic in isolation, using a mocked repository
 * so no real database is needed. Covers the core rules: consecutive days
 * count up, a gap resets the current streak, and "today or yesterday" both
 * keep a streak alive (the grace-window behavior).
 */
@ExtendWith(MockitoExtension.class)
class StreakServiceTest {

    @Mock
    private QuizAttemptRepository quizAttemptRepository;

    @InjectMocks
    private StreakService streakService;

    private final User student = User.builder().id(UUID.randomUUID()).build();

    private QuizAttempt attemptOn(LocalDateTime dateTime) {
        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuiz(new Quiz());
        attempt.setStudent(student);
        attempt.setAttemptedAt(dateTime);
        return attempt;
    }

    @Test
    void noAttempts_returnsZeroStreak() {
        when(quizAttemptRepository.findByStudentId(student.getId())).thenReturn(List.of());

        var result = streakService.forStudent(student);

        assertThat(result.currentStreak()).isEqualTo(0);
        assertThat(result.longestStreak()).isEqualTo(0);
        assertThat(result.lastActiveDate()).isNull();
    }

    @Test
    void threeConsecutiveDaysEndingToday_givesStreakOfThree() {
        LocalDateTime today = LocalDateTime.now();
        List<QuizAttempt> attempts = List.of(
                attemptOn(today.minusDays(2)),
                attemptOn(today.minusDays(1)),
                attemptOn(today)
        );
        when(quizAttemptRepository.findByStudentId(student.getId())).thenReturn(attempts);

        var result = streakService.forStudent(student);

        assertThat(result.currentStreak()).isEqualTo(3);
        assertThat(result.longestStreak()).isEqualTo(3);
    }

    @Test
    void gapInActivity_breaksCurrentStreakButKeepsLongestRecorded() {
        LocalDateTime today = LocalDateTime.now();
        List<QuizAttempt> attempts = List.of(
                // A 4-day run, long ago — establishes "longest"
                attemptOn(today.minusDays(10)),
                attemptOn(today.minusDays(9)),
                attemptOn(today.minusDays(8)),
                attemptOn(today.minusDays(7)),
                // Then a gap, then a single recent day — "current" should
                // reflect only this recent, unbroken run.
                attemptOn(today)
        );
        when(quizAttemptRepository.findByStudentId(student.getId())).thenReturn(attempts);

        var result = streakService.forStudent(student);

        assertThat(result.currentStreak()).isEqualTo(1);
        assertThat(result.longestStreak()).isEqualTo(4);
    }

    @Test
    void lastActivityWasYesterday_streakStillAlive() {
        LocalDateTime today = LocalDateTime.now();
        List<QuizAttempt> attempts = List.of(
                attemptOn(today.minusDays(1))
        );
        when(quizAttemptRepository.findByStudentId(student.getId())).thenReturn(attempts);

        var result = streakService.forStudent(student);

        // This is the grace-window behavior: yesterday still counts as "alive"
        assertThat(result.currentStreak()).isEqualTo(1);
    }

    @Test
    void lastActivityWasTwoDaysAgo_streakIsBroken() {
        LocalDateTime today = LocalDateTime.now();
        List<QuizAttempt> attempts = List.of(
                attemptOn(today.minusDays(2))
        );
        when(quizAttemptRepository.findByStudentId(student.getId())).thenReturn(attempts);

        var result = streakService.forStudent(student);

        assertThat(result.currentStreak()).isEqualTo(0);
        // Longest streak is still recorded, even though it's no longer active
        assertThat(result.longestStreak()).isEqualTo(1);
    }
}