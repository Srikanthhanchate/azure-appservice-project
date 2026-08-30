package com.orginsight.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.orginsight.dto.common.ApiResponse;
import com.orginsight.dto.request.ForgotPasswordRequest;
import com.orginsight.dto.request.LoginRequest;
import com.orginsight.dto.request.RefreshTokenRequest;
import com.orginsight.dto.request.RegisterRequest;
import com.orginsight.dto.request.ResetPasswordRequest;
import com.orginsight.dto.response.LoginResponse;
import com.orginsight.dto.response.TokenResponse;
import com.orginsight.dto.response.UserProfileResponse;
import com.orginsight.service.AuthenticationService;
import com.orginsight.service.SettingsService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final SettingsService settingsService;

    public AuthController(AuthenticationService authenticationService, SettingsService settingsService) {
        this.authenticationService = authenticationService;
        this.settingsService = settingsService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        String message = authenticationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(message, null));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authenticationService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authenticationService.refreshToken(request.getRefreshToken()));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authenticationService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.ok(
                "If an account with that email exists, password reset instructions have been sent.", null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authenticationService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.ok("Password has been reset successfully.", null));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(Authentication authentication) {
        if (authentication != null) {
            authenticationService.logout(authentication.getName());
        }
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully", null));
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> me(Authentication authentication) {
        return ResponseEntity.ok(settingsService.getProfile(authentication.getName()));
    }
}
