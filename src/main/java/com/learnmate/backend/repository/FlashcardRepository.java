package com.learnmate.backend.repository;

import com.learnmate.backend.model.Flashcard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FlashcardRepository extends JpaRepository<Flashcard, UUID> {
    List<Flashcard> findByResourceId(UUID resourceId);
}