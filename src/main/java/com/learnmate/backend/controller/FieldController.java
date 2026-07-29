package com.learnmate.backend.controller;

import com.learnmate.backend.dto.CreateFieldRequest;
import com.learnmate.backend.dto.FieldResponse;
import com.learnmate.backend.service.FieldService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fields")
@RequiredArgsConstructor
public class FieldController {

    private final FieldService fieldService;

    @GetMapping
    public ResponseEntity<List<FieldResponse>> list() {
        return ResponseEntity.ok(fieldService.listFields());
    }

    @PostMapping
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<FieldResponse> create(@Valid @RequestBody CreateFieldRequest request) {
        return ResponseEntity.ok(fieldService.createField(request.name()));
    }
}