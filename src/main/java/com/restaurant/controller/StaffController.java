package com.restaurant.controller;

import com.restaurant.dto.ApiResponse;
import com.restaurant.dto.ChangePasswordRequest;
import com.restaurant.model.StaffUser;
import com.restaurant.repository.StaffUserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * Self-service password management for the currently logged-in staff account.
 * Requires an authenticated session (enforced by SecurityConfig's "/api/**"
 * catch-all), and additionally requires the caller to know their own current
 * password before they can set a new one.
 */
@RestController
@RequestMapping("/api/staff")
public class StaffController {

    private final StaffUserRepository staffUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public StaffController(StaffUserRepository staffUserRepository, PasswordEncoder passwordEncoder) {
        this.staffUserRepository = staffUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PatchMapping("/change-password")
    public ApiResponse<Void> changePassword(Authentication authentication, @Valid @RequestBody ChangePasswordRequest request) {
        StaffUser staff = staffUserRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), staff.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        staff.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        staffUserRepository.save(staff);
        return ApiResponse.ok("Password updated", null);
    }
}
