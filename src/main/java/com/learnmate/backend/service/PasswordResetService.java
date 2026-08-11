package com.learnmate.backend.service;

import com.learnmate.backend.model.PasswordResetToken;
import com.learnmate.backend.model.User;
import com.learnmate.backend.repository.PasswordResetTokenRepository;
import com.learnmate.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Generates a reset token if the email exists. Deliberately does NOT
     * reveal whether the email was found — always returns successfully from
     * the controller's point of view, so this can't be used to check which
     * emails are registered (a real security consideration, not overkill).
     */
    public String requestReset(String email) {
        return userRepository.findByEmail(email)
                .map(user -> {
                    PasswordResetToken resetToken = new PasswordResetToken();
                    resetToken.setUser(user);
                    resetToken.setToken(UUID.randomUUID().toString());
                    resetToken.setExpiresAt(LocalDateTime.now().plusHours(1));
                    tokenRepository.save(resetToken);
                    return resetToken.getToken();
                })
                .orElse(null); // caller decides what to do with null — see controller
    }

    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired reset link"));

        if (resetToken.isUsed()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This reset link has already been used");
        }
        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This reset link has expired");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }
}