package com.learnmate.backend.controller;

import com.learnmate.backend.dto.RecommendationResponse;
import com.learnmate.backend.model.User;
import com.learnmate.backend.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping
    public ResponseEntity<List<RecommendationResponse>> get(@AuthenticationPrincipal User student) {
        return ResponseEntity.ok(recommendationService.forStudent(student));
    }
}