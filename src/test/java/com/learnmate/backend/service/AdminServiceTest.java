package com.learnmate.backend.service;

import com.learnmate.backend.model.Role;
import com.learnmate.backend.model.User;
import com.learnmate.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Tests the admin self-protection guardrails — an admin must not be able to
 * delete their own account or strip their own admin role, which would lock
 * them (and potentially the whole system) out of administrative access.
 */
@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminService adminService;

    private final User admin = User.builder()
            .id(UUID.randomUUID())
            .email("admin@ruh.ac.lk")
            .fullName("Site Admin")
            .role(Role.ADMIN)
            .build();

    @Test
    void adminCannotChangeTheirOwnRole() {
        assertThatThrownBy(() -> adminService.updateRole(admin.getId(), Role.STUDENT, admin))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("your own role");

        // The guardrail must reject before any database write happens
        verify(userRepository, never()).save(any());
    }

    @Test
    void adminCannotDeleteTheirOwnAccount() {
        assertThatThrownBy(() -> adminService.deleteUser(admin.getId(), admin))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("your own account");

        verify(userRepository, never()).delete(any());
    }

    @Test
    void adminCanChangeAnotherUsersRole() {
        User student = User.builder()
                .id(UUID.randomUUID())
                .email("student@example.com")
                .fullName("A Student")
                .role(Role.STUDENT)
                .build();

        when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));

        var result = adminService.updateRole(student.getId(), Role.LECTURER, admin);

        assertThat(result.role()).isEqualTo(Role.LECTURER);
        verify(userRepository).save(student);
    }

    @Test
    void changingRoleOfUnknownUser_isRejected() {
        UUID unknownId = UUID.randomUUID();
        when(userRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.updateRole(unknownId, Role.LECTURER, admin))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User not found");
    }
}