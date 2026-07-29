package com.learnmate.backend.controller;

import com.learnmate.backend.dto.LecturerAnalyticsResponse;
import com.learnmate.backend.dto.StudentAnalyticsResponse;
import com.learnmate.backend.model.User;
import com.learnmate.backend.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/student")
    public ResponseEntity<StudentAnalyticsResponse> studentAnalytics(@AuthenticationPrincipal User student) {
        return ResponseEntity.ok(analyticsService.forStudent(student));
    }

    @GetMapping("/lecturer")
    public ResponseEntity<LecturerAnalyticsResponse> lecturerAnalytics(@AuthenticationPrincipal User lecturer) {
        return ResponseEntity.ok(analyticsService.forLecturer(lecturer));
    }

}