package com.learnmate.backend.dto;

import java.util.UUID;

public record FieldResponse(
        UUID id,
        String name
) {}