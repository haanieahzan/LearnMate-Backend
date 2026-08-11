package com.learnmate.backend.service;

import com.learnmate.backend.dto.ChangePasswordRequest;
import com.learnmate.backend.dto.ProfileResponse;
import com.learnmate.backend.dto.UpdateProfileRequest;
import com.learnmate.backend.model.User;
import com.learnmate.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileResponse getProfile(User user) {
        return toResponse(user);
    }

    public ProfileResponse updateProfile(User user, UpdateProfileRequest request) {
        if (request.fullName() != null && !request.fullName().isBlank()) {
            user.setFullName(request.fullName().trim());
        }
        user.setPhone(request.phone());
        user.setBio(request.bio());
        user.setUniversity(request.university());
        user.setStudentNumber(request.studentNumber());
        user.setDegreeProgramme(request.degreeProgramme());
        user.setYearOfStudy(request.yearOfStudy());
        user.setExpectedGraduation(request.expectedGraduation());

        userRepository.save(user);
        return toResponse(user);
    }

    public void changePassword(User user, ChangePasswordRequest request) {
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    private ProfileResponse toResponse(User u) {
        return new ProfileResponse(
                u.getFullName(), u.getEmail(), u.getRole(), u.getPhone(), u.getBio(),
                u.getUniversity(), u.getStudentNumber(), u.getDegreeProgramme(),
                u.getYearOfStudy(), u.getExpectedGraduation()
        );
    }
}