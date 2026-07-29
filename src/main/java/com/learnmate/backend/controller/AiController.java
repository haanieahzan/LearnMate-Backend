package com.learnmate.backend.controller;

import com.learnmate.backend.dto.AskRequest;
import com.learnmate.backend.model.User;
import com.learnmate.backend.service.AiServiceClient;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiServiceClient aiServiceClient;

    @PostMapping("/ask")
    public ResponseEntity<Map<String, Object>> ask(
            @Valid @RequestBody AskRequest request,
            @AuthenticationPrincipal User user
    ) {
        Map<String, Object> answer = aiServiceClient.ask(
                request.question(), request.resourceId(), request.provider(), request.ollamaModel()
        );
        return ResponseEntity.ok(answer);
    }
}