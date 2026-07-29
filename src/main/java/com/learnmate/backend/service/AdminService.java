package com.learnmate.backend.service;

import com.learnmate.backend.dto.AdminUserResponse;
import com.learnmate.backend.model.Role;
import com.learnmate.backend.model.User;
import com.learnmate.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;

    public List<AdminUserResponse> listUsers() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    public AdminUserResponse updateRole(UUID userId, Role newRole, User actingAdmin) {
        // Guardrail: an admin can't change their own role (would lock themselves
        // out of admin, or accidentally strip their own access mid-session).
        if (userId.equals(actingAdmin.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can't change your own role");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        user.setRole(newRole);
        userRepository.save(user);
        return toResponse(user);
    }

    public void deleteUser(UUID userId, User actingAdmin) {
        // Guardrail: an admin can't delete their own account.
        if (userId.equals(actingAdmin.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can't delete your own account");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        try {
            userRepository.delete(user);
        } catch (Exception e) {
            // A lecturer who owns courses (or a student with enrollments) can't be
            // deleted while those foreign-key references exist. Surface a clear
            // message instead of a raw 500.
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This user still has courses or other records linked to them and can't be deleted.");
        }
    }

    private AdminUserResponse toResponse(User u) {
        return new AdminUserResponse(u.getId(), u.getEmail(), u.getFullName(), u.getRole(), u.getCreatedAt());
    }
}