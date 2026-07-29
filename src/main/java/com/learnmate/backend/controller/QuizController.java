package com.learnmate.backend.controller;

import com.learnmate.backend.dto.GenerateQuizRequest;
import com.learnmate.backend.dto.QuizResponse;
import com.learnmate.backend.service.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.learnmate.backend.dto.QuizAttemptResponse;
import com.learnmate.backend.dto.SubmitQuizAttemptRequest;
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
    public ResponseEntity<List<QuizResponse>> listByCourse(@PathVariable UUID courseId) {
        return ResponseEntity.ok(quizService.listByCourse(courseId));
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
}