package com.learnmate.backend.service;

import com.learnmate.backend.dto.StreakResponse;
import com.learnmate.backend.model.User;
import com.learnmate.backend.repository.QuizAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.TreeSet;

@Service
@RequiredArgsConstructor
public class StreakService {

    private final QuizAttemptRepository quizAttemptRepository;

    public StreakResponse forStudent(User student) {
        // Distinct calendar dates the student attempted at least one quiz on,
        // sorted ascending. A TreeSet naturally dedupes and orders.
        TreeSet<LocalDate> activeDates = new TreeSet<>();
        quizAttemptRepository.findByStudentId(student.getId())
                .forEach(a -> activeDates.add(a.getAttemptedAt().toLocalDate()));

        if (activeDates.isEmpty()) {
            return new StreakResponse(0, 0, null);
        }

        List<LocalDate> sorted = activeDates.stream().toList();

        // Longest streak ever: walk forward, reset the run whenever there's a gap.
        int longest = 1;
        int run = 1;
        for (int i = 1; i < sorted.size(); i++) {
            if (sorted.get(i).equals(sorted.get(i - 1).plusDays(1))) {
                run++;
            } else {
                run = 1;
            }
            longest = Math.max(longest, run);
        }

        // Current streak: only "alive" if the most recent active day was today
        // or yesterday — otherwise the streak has been broken.
        LocalDate lastActive = sorted.get(sorted.size() - 1);
        LocalDate today = LocalDate.now();
        int current = 0;

        if (!lastActive.isBefore(today.minusDays(1))) {
            current = 1;
            LocalDate cursor = lastActive;
            for (int i = sorted.size() - 2; i >= 0; i--) {
                if (sorted.get(i).equals(cursor.minusDays(1))) {
                    current++;
                    cursor = sorted.get(i);
                } else {
                    break;
                }
            }
        }

        return new StreakResponse(current, longest, lastActive);
    }
}