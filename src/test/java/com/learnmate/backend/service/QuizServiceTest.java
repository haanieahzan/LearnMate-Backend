package com.learnmate.backend.service;

import com.learnmate.backend.dto.SubmitQuizAttemptRequest;
import com.learnmate.backend.model.Quiz;
import com.learnmate.backend.model.QuizQuestion;
import com.learnmate.backend.model.User;
import com.learnmate.backend.repository.QuizAttemptRepository;
import com.learnmate.backend.repository.QuizQuestionRepository;
import com.learnmate.backend.repository.QuizRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Tests the server-side quiz grading logic — the piece where a bug would
 * directly produce a wrong score for a student. Repositories are mocked so
 * no database is involved; only the grading maths and correctness rules
 * are under test here.
 */
@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

    @Mock private QuizRepository quizRepository;
    @Mock private QuizQuestionRepository quizQuestionRepository;
    @Mock private QuizAttemptRepository quizAttemptRepository;

    @InjectMocks
    private QuizService quizService;

    private Quiz quiz;
    private User student;
    private QuizQuestion q1;
    private QuizQuestion q2;
    private QuizQuestion q3;
    private QuizQuestion q4;

    @BeforeEach
    void setUp() {
        quiz = new Quiz();
        quiz.setId(UUID.randomUUID());

        student = User.builder().id(UUID.randomUUID()).build();

        q1 = question("What is a class?", "A blueprint for objects");
        q2 = question("What is encapsulation?", "Hiding internal state");
        q3 = question("What is inheritance?", "Reusing behaviour from a parent");
        q4 = question("What is polymorphism?", "One interface, many forms");
    }

    private QuizQuestion question(String text, String correctAnswer) {
        QuizQuestion q = new QuizQuestion();
        q.setId(UUID.randomUUID());
        q.setQuiz(quiz);
        q.setQuestionText(text);
        q.setCorrectAnswer(correctAnswer);
        q.setOptions("[]");
        return q;
    }

    @Test
    void allAnswersCorrect_scoresOneHundredPercent() {
        when(quizRepository.findById(quiz.getId())).thenReturn(Optional.of(quiz));
        when(quizQuestionRepository.findByQuizId(quiz.getId())).thenReturn(List.of(q1, q2, q3, q4));

        var request = new SubmitQuizAttemptRequest(Map.of(
                q1.getId(), "A blueprint for objects",
                q2.getId(), "Hiding internal state",
                q3.getId(), "Reusing behaviour from a parent",
                q4.getId(), "One interface, many forms"
        ));

        var result = quizService.submitAttempt(quiz.getId(), request, student);

        assertThat(result.correctCount()).isEqualTo(4);
        assertThat(result.totalQuestions()).isEqualTo(4);
        assertThat(result.score()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
    }

    @Test
    void halfCorrect_scoresFiftyPercent() {
        when(quizRepository.findById(quiz.getId())).thenReturn(Optional.of(quiz));
        when(quizQuestionRepository.findByQuizId(quiz.getId())).thenReturn(List.of(q1, q2, q3, q4));

        var request = new SubmitQuizAttemptRequest(Map.of(
                q1.getId(), "A blueprint for objects",     // correct
                q2.getId(), "Hiding internal state",        // correct
                q3.getId(), "Something wrong",              // wrong
                q4.getId(), "Also wrong"                    // wrong
        ));

        var result = quizService.submitAttempt(quiz.getId(), request, student);

        assertThat(result.correctCount()).isEqualTo(2);
        assertThat(result.score()).isEqualByComparingTo(BigDecimal.valueOf(50.00));
    }

    @Test
    void unansweredQuestionsCountAsWrong_notAsErrors() {
        when(quizRepository.findById(quiz.getId())).thenReturn(Optional.of(quiz));
        when(quizQuestionRepository.findByQuizId(quiz.getId())).thenReturn(List.of(q1, q2));

        // Only one of the two questions is answered — the other is simply absent
        var request = new SubmitQuizAttemptRequest(Map.of(
                q1.getId(), "A blueprint for objects"
        ));

        var result = quizService.submitAttempt(quiz.getId(), request, student);

        assertThat(result.correctCount()).isEqualTo(1);
        assertThat(result.score()).isEqualByComparingTo(BigDecimal.valueOf(50.00));
        // The unanswered one should still appear in the breakdown, marked wrong
        assertThat(result.breakdown()).hasSize(2);
        assertThat(result.breakdown().get(1).selectedAnswer()).isNull();
        assertThat(result.breakdown().get(1).correct()).isFalse();
    }

    @Test
    void answerMatchingIsCaseInsensitiveAndIgnoresSurroundingWhitespace() {
        when(quizRepository.findById(quiz.getId())).thenReturn(Optional.of(quiz));
        when(quizQuestionRepository.findByQuizId(quiz.getId())).thenReturn(List.of(q1));

        // Same answer, different casing and padded with spaces — should still
        // be graded correct, since AI-generated options can carry stray
        // whitespace and we don't want that to penalise a student.
        var request = new SubmitQuizAttemptRequest(Map.of(
                q1.getId(), "   a BLUEPRINT for Objects  "
        ));

        var result = quizService.submitAttempt(quiz.getId(), request, student);

        assertThat(result.correctCount()).isEqualTo(1);
        assertThat(result.score()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
    }

    @Test
    void allAnswersWrong_scoresZero() {
        when(quizRepository.findById(quiz.getId())).thenReturn(Optional.of(quiz));
        when(quizQuestionRepository.findByQuizId(quiz.getId())).thenReturn(List.of(q1, q2));

        var request = new SubmitQuizAttemptRequest(Map.of(
                q1.getId(), "Nope",
                q2.getId(), "Also nope"
        ));

        var result = quizService.submitAttempt(quiz.getId(), request, student);

        assertThat(result.correctCount()).isEqualTo(0);
        assertThat(result.score()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void quizWithNoQuestions_isRejected() {
        when(quizRepository.findById(quiz.getId())).thenReturn(Optional.of(quiz));
        when(quizQuestionRepository.findByQuizId(quiz.getId())).thenReturn(List.of());

        var request = new SubmitQuizAttemptRequest(Map.of());

        assertThatThrownBy(() -> quizService.submitAttempt(quiz.getId(), request, student))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("no questions");
    }

    @Test
    void submittingToNonExistentQuiz_isRejected() {
        UUID unknownId = UUID.randomUUID();
        when(quizRepository.findById(unknownId)).thenReturn(Optional.empty());

        var request = new SubmitQuizAttemptRequest(Map.of());

        assertThatThrownBy(() -> quizService.submitAttempt(unknownId, request, student))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Quiz not found");
    }
}