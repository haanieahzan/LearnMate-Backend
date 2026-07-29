package com.learnmate.backend.dto;

import java.util.List;
import java.util.UUID;

public record QuizQuestionResponse(
        UUID id,
        String questionText,
        List<String> options
        // Deliberately no correctAnswer here — never send the answer key to
        // the student while they're taking the quiz.
) {}