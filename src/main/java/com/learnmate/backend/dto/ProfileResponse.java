package com.learnmate.backend.dto;

import com.learnmate.backend.model.Role;

public record ProfileResponse(
        String fullName,
        String email,
        Role role,
        String phone,
        String bio,
        String university,
        String studentNumber,
        String degreeProgramme,
        Integer yearOfStudy,
        String expectedGraduation
) {}