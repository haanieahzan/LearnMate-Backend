package com.learnmate.backend.controller;

import com.learnmate.backend.dto.FlashcardResponse;
import com.learnmate.backend.dto.GenerateFlashcardsRequest;
import com.learnmate.backend.service.FlashcardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/flashcards")
@RequiredArgsConstructor
public class FlashcardController {

    private final FlashcardService flashcardService;

    @PostMapping("/generate")
    public ResponseEntity<List<FlashcardResponse>> generate(@Valid @RequestBody GenerateFlashcardsRequest request) {
        return ResponseEntity.ok(flashcardService.generate(request));
    }

    @GetMapping("/resource/{resourceId}")
    public ResponseEntity<List<FlashcardResponse>> listByResource(@PathVariable UUID resourceId) {
        return ResponseEntity.ok(flashcardService.listByResource(resourceId));
    }
}