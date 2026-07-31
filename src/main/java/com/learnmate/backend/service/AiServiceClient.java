package com.learnmate.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnmate.backend.exception.AiQuotaExceededException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class AiServiceClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiServiceClient(@Value("${ai.service.url}") String aiServiceUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(aiServiceUrl)
                .requestFactory(new org.springframework.http.client.SimpleClientHttpRequestFactory())
                .build();
    }

    public void ingestResource(UUID resourceId, String filePath) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("resource_id", resourceId.toString());
            payload.put("file_path", filePath);
            String json = objectMapper.writeValueAsString(payload);

            restClient.post()
                    .uri("/ingest")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Ingested resource {} into AI service", resourceId);
        } catch (Exception e) {
            log.error("AI ingestion failed for resource {}: {}", resourceId, e.getMessage());
        }
    }

    public Map<String, Object> ask(String question, UUID resourceId, String provider, String ollamaModel) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("question", question);
            payload.put("resource_id", resourceId != null ? resourceId.toString() : null);
            payload.put("provider", provider);
            payload.put("ollama_model", ollamaModel);
            String json = objectMapper.writeValueAsString(payload);

            return restClient.post()
                    .uri("/ask")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json)
                    .retrieve()
                    .body(Map.class);
        } catch (HttpClientErrorException.TooManyRequests e) {
            throw new AiQuotaExceededException(extractQuotaMessage(e));
        } catch (Exception e) {
            log.error("AI ask failed: {}", e.getMessage());
            throw new RuntimeException("AI service request failed", e);
        }
    }

    public Map<String, Object> generateQuiz(UUID resourceId, int numQuestions, String difficulty, String provider, String ollamaModel) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("resource_id", resourceId.toString());
            payload.put("num_questions", numQuestions);
            payload.put("difficulty", difficulty);
            payload.put("provider", provider);
            payload.put("ollama_model", ollamaModel);
            String json = objectMapper.writeValueAsString(payload);

            return restClient.post()
                    .uri("/generate-quiz")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json)
                    .retrieve()
                    .body(Map.class);
        } catch (HttpClientErrorException.TooManyRequests e) {
            throw new AiQuotaExceededException(extractQuotaMessage(e));
        } catch (Exception e) {
            log.error("Quiz generation failed: {}", e.getMessage());
            throw new RuntimeException("Quiz generation failed", e);
        }
    }

    /** Reads the AI service's current provider/model config. */
    public Map<String, Object> getLlmConfig() {
        try {
            return restClient.get()
                    .uri("/admin/llm-config")
                    .retrieve()
                    .body(Map.class);
        } catch (Exception e) {
            log.error("Could not read AI config: {}", e.getMessage());
            throw new RuntimeException("Could not reach the AI service", e);
        }
    }

    /** Switches the AI service between "gemini" and "ollama" at runtime. */
    public Map<String, Object> setLlmProvider(String provider) {
        try {
            return restClient.post()
                    .uri(uriBuilder -> uriBuilder.path("/admin/llm-provider")
                            .queryParam("provider", provider).build())
                    .retrieve()
                    .body(Map.class);
        } catch (Exception e) {
            log.error("Could not switch AI provider: {}", e.getMessage());
            throw new RuntimeException("Could not switch the AI provider", e);
        }
    }

    /** Switches which local Ollama model is used for generation. */
    public Map<String, Object> setOllamaModel(String model) {
        try {
            return restClient.post()
                    .uri(uriBuilder -> uriBuilder.path("/admin/ollama-model")
                            .queryParam("model", model).build())
                    .retrieve()
                    .body(Map.class);
        } catch (Exception e) {
            log.error("Could not switch Ollama model: {}", e.getMessage());
            throw new RuntimeException("Could not switch the local model", e);
        }
    }

    private String extractQuotaMessage(HttpClientErrorException.TooManyRequests e) {
        try {
            JsonNode node = objectMapper.readTree(e.getResponseBodyAsString());
            return node.path("detail").asText("AI service quota exceeded. Please switch the AI provider.");
        } catch (Exception parseEx) {
            return "AI service quota exceeded. Please switch the AI provider.";
        }
    }
    public Map<String, Object> generateFlashcards(UUID resourceId, int numCards, String provider, String ollamaModel) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("resource_id", resourceId.toString());
            payload.put("num_cards", numCards);
            payload.put("provider", provider);
            payload.put("ollama_model", ollamaModel);
            String json = objectMapper.writeValueAsString(payload);

            return restClient.post()
                    .uri("/generate-flashcards")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json)
                    .retrieve()
                    .body(Map.class);
        } catch (Exception e) {
            log.error("Flashcard generation failed: {}", e.getMessage());
            throw new RuntimeException("Flashcard generation failed", e);
        }
    }

}