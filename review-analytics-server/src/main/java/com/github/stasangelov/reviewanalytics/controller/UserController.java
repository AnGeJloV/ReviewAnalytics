package com.github.stasangelov.reviewanalytics.controller;

import com.github.stasangelov.reviewanalytics.dto.UserManagementDto;
import com.github.stasangelov.reviewanalytics.entity.Role;
import com.github.stasangelov.reviewanalytics.entity.User;
import com.github.stasangelov.reviewanalytics.repository.UserRepository;
import com.github.stasangelov.reviewanalytics.service.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class UserController {

    private final UserServiceImpl userService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<UserManagementDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<UserManagementDto> changeRole(@PathVariable Long id,
                                                        @RequestBody Map<String, String> roleUpdate) {
        User currentUser = getCurrentUser();
        Role.RoleName newRole = Role.RoleName.valueOf(roleUpdate.get("role").toUpperCase());
        return ResponseEntity.ok(userService.changeUserRole(id, newRole, currentUser));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<UserManagementDto> changeStatus(@PathVariable Long id,
                                                          @RequestBody Map<String, Boolean> statusUpdate) {
        User currentUser = getCurrentUser();
        boolean newStatus = statusUpdate.get("active");
        return ResponseEntity.ok(userService.changeUserStatus(id, newStatus, currentUser));
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        return userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new RuntimeException("Текущий аутентифицированный пользователь не найден в базе данных"));
    }
}
