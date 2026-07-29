package com.learnmate.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnmate.backend.dto.*;
import com.learnmate.backend.model.*;
import com.learnmate.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.math.RoundingMode;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final LearningResourceRepository resourceRepository;
    private final AiServiceClient aiServiceClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final QuizAttemptRepository quizAttemptRepository;

    @SuppressWarnings("unchecked")
    public QuizResponse generateQuiz(GenerateQuizRequest request) {
        LearningResource resource = resourceRepository.findById(request.resourceId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found"));

        int numQuestions = request.numQuestions() > 0 ? request.numQuestions() : 5;
        String difficulty = (request.difficulty() != null && !request.difficulty().isBlank())
                ? request.difficulty() : "Medium";
        Map<String, Object> aiResult = aiServiceClient.generateQuiz(
                resource.getId(), numQuestions, difficulty, request.provider(), request.ollamaModel()
        );

        List<Map<String, Object>> rawQuestions = (List<Map<String, Object>>) aiResult.get("questions");
        String skillLabel = (String) aiResult.get("skill_label");
        if (rawQuestions == null || rawQuestions.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Couldn't generate a quiz — this document may not have enough ingested content yet.");
        }

        Quiz quiz = new Quiz();
        quiz.setCourse(resource.getCourse());
        quiz.setResource(resource);
        quiz.setTitle("Quiz: " + resource.getTitle() + " (" + difficulty + ")");
        quiz.setSkillLabel(skillLabel != null && !skillLabel.isBlank() ? skillLabel : resource.getTitle());
        quiz.setGeneratedBy("AI");
        quizRepository.save(quiz);

        for (Map<String, Object> q : rawQuestions) {
            try {
                QuizQuestion question = new QuizQuestion();
                question.setQuiz(quiz);
                question.setQuestionText((String) q.get("question_text"));
                question.setQuestionType("MCQ");
                question.setOptions(objectMapper.writeValueAsString(q.get("options")));
                question.setCorrectAnswer((String) q.get("correct_answer"));
                quizQuestionRepository.save(question);
            } catch (Exception e) {
                // Skip a single malformed question rather than failing the
                // whole quiz — AI-generated JSON occasionally has one bad
                // entry even when the rest is fine.
            }
        }

        return toResponse(quiz);
    }

    public List<QuizResponse> listByCourse(UUID courseId) {
        return quizRepository.findByCourseId(courseId).stream().map(this::toResponse).toList();
    }

    public void deleteQuiz(UUID quizId) {
        if (!quizRepository.existsById(quizId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found");
        }
        quizRepository.deleteById(quizId);
    }

    private QuizResponse toResponse(Quiz quiz) {
        List<QuizQuestionResponse> questions = quizQuestionRepository.findByQuizId(quiz.getId())
                .stream()
                .map(this::toQuestionResponse)
                .toList();
        return new QuizResponse(quiz.getId(), quiz.getCourse().getId(), quiz.getTitle(), quiz.getCreatedAt(), questions);
    }

    @SuppressWarnings("unchecked")
    private QuizQuestionResponse toQuestionResponse(QuizQuestion q) {
        try {
            List<String> options = objectMapper.readValue(q.getOptions(), List.class);
            return new QuizQuestionResponse(q.getId(), q.getQuestionText(), options);
        } catch (Exception e) {
            return new QuizQuestionResponse(q.getId(), q.getQuestionText(), List.of());
        }
    }

    public QuizAttemptResponse submitAttempt(UUID quizId, SubmitQuizAttemptRequest request, User student) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found"));

        List<QuizQuestion> questions = quizQuestionRepository.findByQuizId(quizId);
        if (questions.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "This quiz has no questions");
        }

        List<QuestionResult> breakdown = new java.util.ArrayList<>();
        int correctCount = 0;

        for (QuizQuestion q : questions) {
            String selected = request.answers().get(q.getId());
            // Server decides correctness — the client's opinion is never trusted.
            boolean isCorrect = selected != null && selected.trim().equalsIgnoreCase(q.getCorrectAnswer().trim());
            if (isCorrect) correctCount++;

            breakdown.add(new QuestionResult(
                    q.getId(), q.getQuestionText(), selected, q.getCorrectAnswer(), isCorrect
            ));
        }

        BigDecimal score = BigDecimal.valueOf(correctCount)
                .divide(BigDecimal.valueOf(questions.size()), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuiz(quiz);
        attempt.setStudent(student);
        attempt.setScore(score);
        quizAttemptRepository.save(attempt);

        return new QuizAttemptResponse(
                attempt.getId(), quiz.getId(), score, correctCount, questions.size(),
                attempt.getAttemptedAt(), breakdown
        );
    }

    public List<QuizAttemptResponse> myAttempts(User student) {
        return quizAttemptRepository.findByStudentId(student.getId()).stream()
                .map(a -> new QuizAttemptResponse(
                        a.getId(), a.getQuiz().getId(), a.getScore(), 0, 0, a.getAttemptedAt(), List.of()
                ))
                .toList();
    }
}