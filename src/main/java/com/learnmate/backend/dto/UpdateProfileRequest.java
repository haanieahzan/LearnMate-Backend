package com.learnmate.backend.dto;

public record UpdateProfileRequest(
        String fullName,
        String phone,
        String bio,
        String university,
        String studentNumber,
        String degreeProgramme,
        Integer yearOfStudy,
        String expectedGraduation
) {}