package com.learnmate.backend.controller;

import com.learnmate.backend.dto.CourseResponse;
import com.learnmate.backend.dto.CreateCourseRequest;
import com.learnmate.backend.model.User;
import com.learnmate.backend.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<CourseResponse> createCourse(
            @Valid @RequestBody CreateCourseRequest request,
            @AuthenticationPrincipal User lecturer
    ) {
        return ResponseEntity.ok(courseService.createCourse(request, lecturer));
    }

    @GetMapping
    public ResponseEntity<List<CourseResponse>> listCourses() {
        return ResponseEntity.ok(courseService.listCourses());
    }


    @GetMapping("/{courseId}")
    public ResponseEntity<CourseResponse> getCourse(@PathVariable UUID courseId) {
        return ResponseEntity.ok(courseService.getCourse(courseId));
    }

    @DeleteMapping("/{courseId}")
    @PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
    public ResponseEntity<Void> delete(
            @PathVariable UUID courseId,
            @AuthenticationPrincipal User actingUser
    ) {
        courseService.deleteCourse(courseId, actingUser);
        return ResponseEntity.noContent().build();
    }

}