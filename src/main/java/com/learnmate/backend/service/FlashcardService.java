package com.learnmate.backend.service;

import com.learnmate.backend.dto.FlashcardResponse;
import com.learnmate.backend.dto.GenerateFlashcardsRequest;
import com.learnmate.backend.model.Flashcard;
import com.learnmate.backend.model.LearningResource;
import com.learnmate.backend.repository.FlashcardRepository;
import com.learnmate.backend.repository.LearningResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FlashcardService {

    private final FlashcardRepository flashcardRepository;
    private final LearningResourceRepository resourceRepository;
    private final AiServiceClient aiServiceClient;

    @SuppressWarnings("unchecked")
    public List<FlashcardResponse> generate(GenerateFlashcardsRequest request) {
        LearningResource resource = resourceRepository.findById(request.resourceId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found"));

        int numCards = request.numCards() > 0 ? request.numCards() : 10;
        Map<String, Object> aiResult = aiServiceClient.generateFlashcards(
                resource.getId(), numCards, request.provider(), request.ollamaModel()
        );

        List<Map<String, String>> rawCards = (List<Map<String, String>>) aiResult.get("cards");
        if (rawCards == null || rawCards.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Couldn't generate flashcards — this document may not have enough ingested content yet.");
        }

        List<Flashcard> saved = rawCards.stream().map(c -> {
            Flashcard card = new Flashcard();
            card.setResource(resource);
            card.setFrontText(c.get("front"));
            card.setBackText(c.get("back"));
            flashcardRepository.save(card);
            return card;
        }).toList();

        return saved.stream().map(this::toResponse).toList();
    }

    public List<FlashcardResponse> listByResource(UUID resourceId) {
        return flashcardRepository.findByResourceId(resourceId).stream().map(this::toResponse).toList();
    }

    public void deleteAllForResource(UUID resourceId) {
        flashcardRepository.deleteAll(flashcardRepository.findByResourceId(resourceId));
    }

    private FlashcardResponse toResponse(Flashcard f) {
        return new FlashcardResponse(f.getId(), f.getFrontText(), f.getBackText());
    }
}