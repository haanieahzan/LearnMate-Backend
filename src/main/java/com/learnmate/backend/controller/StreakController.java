package com.learnmate.backend.controller;

import com.learnmate.backend.dto.StreakResponse;
import com.learnmate.backend.model.User;
import com.learnmate.backend.service.StreakService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/streaks")
@RequiredArgsConstructor
public class StreakController {

    private final StreakService streakService;

    @GetMapping
    public ResponseEntity<StreakResponse> get(@AuthenticationPrincipal User student) {
        return ResponseEntity.ok(streakService.forStudent(student));
    }
}