package com.learnmate.backend.controller;

import com.learnmate.backend.dto.AnnouncementResponse;
import com.learnmate.backend.dto.CreateAnnouncementRequest;
import com.learnmate.backend.model.User;
import com.learnmate.backend.service.AnnouncementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/courses/{courseId}/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @PostMapping
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<AnnouncementResponse> create(
            @PathVariable UUID courseId,
            @Valid @RequestBody CreateAnnouncementRequest request,
            @AuthenticationPrincipal User poster
    ) {
        return ResponseEntity.ok(announcementService.create(courseId, request, poster));
    }

    @GetMapping
    public ResponseEntity<List<AnnouncementResponse>> list(@PathVariable UUID courseId) {
        return ResponseEntity.ok(announcementService.listByCourse(courseId));
    }

    @DeleteMapping("/{announcementId}")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<Void> delete(
            @PathVariable UUID courseId,
            @PathVariable UUID announcementId
    ) {
        announcementService.delete(announcementId);
        return ResponseEntity.noContent().build();
    }
}