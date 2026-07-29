package com.learnmate.backend.service;

import com.learnmate.backend.dto.FieldResponse;
import com.learnmate.backend.model.Field;
import com.learnmate.backend.repository.FieldRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FieldService {

    private final FieldRepository fieldRepository;

    public List<FieldResponse> listFields() {
        return fieldRepository.findAll().stream()
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .map(f -> new FieldResponse(f.getId(), f.getName()))
                .toList();
    }

    public FieldResponse createField(String name) {
        String trimmed = name.trim();

        // Case-insensitive duplicate check — if "AI" already exists and someone
        // types "ai", reuse the existing one instead of creating a near-duplicate.
        Field existing = fieldRepository.findAll().stream()
                .filter(f -> f.getName().equalsIgnoreCase(trimmed))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            return new FieldResponse(existing.getId(), existing.getName());
        }

        Field field = new Field();
        field.setName(trimmed);
        fieldRepository.save(field);
        return new FieldResponse(field.getId(), field.getName());
    }
}