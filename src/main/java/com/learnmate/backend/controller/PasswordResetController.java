package com.learnmate.backend.controller;

import com.learnmate.backend.dto.ForgotPasswordRequest;
import com.learnmate.backend.dto.ResetPasswordRequest;
import com.learnmate.backend.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        String token = passwordResetService.requestReset(request.email());

        // Demo-mode: return the token directly instead of emailing it, so the
        // frontend can show a reset link on screen. A production version would
        // email this token instead of returning it in the API response.
        Map<String, String> response = new java.util.HashMap<>();
        response.put("message", "If that email is registered, a reset link has been generated below.");
        if (token != null) {
            response.put("resetToken", token);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok(Map.of("message", "Password reset successfully. You can now log in."));
    }
}