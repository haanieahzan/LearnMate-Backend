package com.learnmate.backend.controller;

import com.learnmate.backend.dto.*;
import com.learnmate.backend.model.Role;
import com.learnmate.backend.service.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.learnmate.backend.model.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @PostMapping("/generate")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<QuizResponse> generate(@Valid @RequestBody GenerateQuizRequest request) {
        return ResponseEntity.ok(quizService.generateQuiz(request));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<QuizResponse>> listByCourse(
            @PathVariable UUID courseId,
            @AuthenticationPrincipal User user
    ) {
        boolean includeUnpublished = user.getRole() == Role.LECTURER;
        return ResponseEntity.ok(quizService.listByCourse(courseId, includeUnpublished));
    }

    @DeleteMapping("/{quizId}")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<Void> delete(@PathVariable UUID quizId) {
        quizService.deleteQuiz(quizId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{quizId}/attempts")
    public ResponseEntity<QuizAttemptResponse> submitAttempt(
            @PathVariable UUID quizId,
            @Valid @RequestBody SubmitQuizAttemptRequest request,
            @AuthenticationPrincipal User student
    ) {
        return ResponseEntity.ok(quizService.submitAttempt(quizId, request, student));
    }

    @GetMapping("/attempts/me")
    public ResponseEntity<List<QuizAttemptResponse>> myAttempts(@AuthenticationPrincipal User student) {
        return ResponseEntity.ok(quizService.myAttempts(student));
    }

    @GetMapping("/search")
    public ResponseEntity<List<QuizResponse>> search(
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String format
    ) {
        return ResponseEntity.ok(quizService.search(topic, format));
    }

    @GetMapping("/{quizId}/review")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<List<QuizQuestionReviewResponse>> reviewQuestions(
            @PathVariable UUID quizId,
            @AuthenticationPrincipal User lecturer
    ) {
        return ResponseEntity.ok(quizService.getQuestionsForReview(quizId, lecturer));
    }

    @PutMapping("/questions/{questionId}")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<Void> updateQuestion(
            @PathVariable UUID questionId,
            @Valid @RequestBody UpdateQuestionRequest request,
            @AuthenticationPrincipal User lecturer
    ) {
        quizService.updateQuestion(questionId, request, lecturer);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/questions/{questionId}")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable UUID questionId,
            @AuthenticationPrincipal User lecturer
    ) {
        quizService.deleteQuestion(questionId, lecturer);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{quizId}/publish")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<QuizResponse> publish(
            @PathVariable UUID quizId,
            @AuthenticationPrincipal User lecturer
    ) {
        return ResponseEntity.ok(quizService.publish(quizId, lecturer));
    }
}