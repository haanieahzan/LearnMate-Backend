package com.learnmate.backend.controller;

import com.learnmate.backend.dto.FieldScore;
import com.learnmate.backend.dto.QuizAttemptSummary;
import com.learnmate.backend.dto.SkillAreaScore;
import com.learnmate.backend.model.User;
import com.learnmate.backend.service.SkillsAssessmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/skills-assessments")
@RequiredArgsConstructor
public class SkillsAssessmentController {

    private final SkillsAssessmentService assessmentService;

    @GetMapping("/current")
    public ResponseEntity<List<FieldScore>> current(@AuthenticationPrincipal User student) {
        return ResponseEntity.ok(assessmentService.currentSkillsByField(student));
    }

    @GetMapping("/history")
    public ResponseEntity<List<QuizAttemptSummary>> history(@AuthenticationPrincipal User student) {
        return ResponseEntity.ok(assessmentService.history(student));
    }
}