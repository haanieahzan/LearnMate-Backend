package com.learnmate.backend.controller;

import com.learnmate.backend.dto.LearningResourceResponse;
import com.learnmate.backend.model.User;
import com.learnmate.backend.service.LearningResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/courses/{courseId}/resources")
@RequiredArgsConstructor
public class LearningResourceController {

    private final LearningResourceService resourceService;

    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<LearningResourceResponse> upload(
            @PathVariable UUID courseId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User uploader
    ) {
        return ResponseEntity.ok(resourceService.uploadResource(courseId, file, uploader));
    }

    @GetMapping
    public ResponseEntity<List<LearningResourceResponse>> list(@PathVariable UUID courseId) {
        return ResponseEntity.ok(resourceService.listByCourse(courseId));
    }

    @GetMapping("/{resourceId}/download")
    public ResponseEntity<Resource> download(
            @PathVariable UUID courseId,
            @PathVariable UUID resourceId
    ) {
        return resourceService.loadFileForDownload(resourceId);
    }

    @DeleteMapping("/{resourceId}")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<Void> delete(
            @PathVariable UUID courseId,
            @PathVariable UUID resourceId
    ) {
        resourceService.deleteResource(resourceId);
        return ResponseEntity.noContent().build();
    }

}