package com.learnmate.backend.controller;

import com.learnmate.backend.dto.ChangePasswordRequest;
import com.learnmate.backend.dto.ProfileResponse;
import com.learnmate.backend.dto.UpdateProfileRequest;
import com.learnmate.backend.model.User;
import com.learnmate.backend.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<ProfileResponse> get(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(profileService.getProfile(user));
    }

    @PutMapping
    public ResponseEntity<ProfileResponse> update(
            @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(profileService.updateProfile(user, request));
    }

    @PutMapping("/password")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal User user
    ) {
        profileService.changePassword(user, request);
        return ResponseEntity.ok(Map.of("message", "Password updated successfully."));
    }
}