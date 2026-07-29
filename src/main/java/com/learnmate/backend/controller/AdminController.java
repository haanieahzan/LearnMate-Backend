package com.learnmate.backend.controller;

import com.learnmate.backend.dto.AdminUserResponse;
import com.learnmate.backend.dto.UpdateRoleRequest;
import com.learnmate.backend.model.User;
import com.learnmate.backend.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")   // applies to every method in this controller
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping
    public ResponseEntity<List<AdminUserResponse>> listUsers() {
        return ResponseEntity.ok(adminService.listUsers());
    }

    @PatchMapping("/{userId}/role")
    public ResponseEntity<AdminUserResponse> updateRole(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateRoleRequest request,
            @AuthenticationPrincipal User actingAdmin
    ) {
        return ResponseEntity.ok(adminService.updateRole(userId, request.role(), actingAdmin));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable UUID userId,
            @AuthenticationPrincipal User actingAdmin
    ) {
        adminService.deleteUser(userId, actingAdmin);
        return ResponseEntity.noContent().build();
    }
}