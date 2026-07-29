package com.learnmate.backend.controller;

import com.learnmate.backend.service.AiServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/ai-config")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AiConfigController {

    private final AiServiceClient aiServiceClient;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getConfig() {
        return ResponseEntity.ok(aiServiceClient.getLlmConfig());
    }

    @PostMapping("/provider")
    public ResponseEntity<Map<String, Object>> setProvider(@RequestParam String provider) {
        return ResponseEntity.ok(aiServiceClient.setLlmProvider(provider));
    }

    @PostMapping("/model")
    public ResponseEntity<Map<String, Object>> setModel(@RequestParam String model) {
        return ResponseEntity.ok(aiServiceClient.setOllamaModel(model));
    }
}