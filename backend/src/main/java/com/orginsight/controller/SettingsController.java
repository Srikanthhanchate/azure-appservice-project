package com.orginsight.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.orginsight.dto.common.ApiResponse;
import com.orginsight.dto.request.ChangePasswordRequest;
import com.orginsight.dto.request.NotificationSettingsRequest;
import com.orginsight.dto.request.ProfileUpdateRequest;
import com.orginsight.dto.request.SecuritySettingsRequest;
import com.orginsight.dto.request.ThemeSettingsRequest;
import com.orginsight.dto.response.UserProfileResponse;
import com.orginsight.service.SettingsService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final SettingsService settingsService;

    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(Authentication authentication) {
        return ResponseEntity.ok(settingsService.getProfile(authentication.getName()));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(Authentication authentication,
                                                               @Valid @RequestBody ProfileUpdateRequest request) {
        return ResponseEntity.ok(settingsService.updateProfile(authentication.getName(), request));
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(Authentication authentication,
                                                              @Valid @RequestBody ChangePasswordRequest request) {
        settingsService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.ok("Password changed successfully", null));
    }

    @PutMapping("/notifications")
    public ResponseEntity<UserProfileResponse> updateNotifications(Authentication authentication,
                                                                     @RequestBody NotificationSettingsRequest request) {
        return ResponseEntity.ok(settingsService.updateNotificationSettings(authentication.getName(), request));
    }

    @PutMapping("/theme")
    public ResponseEntity<UserProfileResponse> updateTheme(Authentication authentication,
                                                             @Valid @RequestBody ThemeSettingsRequest request) {
        return ResponseEntity.ok(settingsService.updateThemeSettings(authentication.getName(), request));
    }

    @PutMapping("/security")
    public ResponseEntity<UserProfileResponse> updateSecurity(Authentication authentication,
                                                                @RequestBody SecuritySettingsRequest request) {
        return ResponseEntity.ok(settingsService.updateSecuritySettings(authentication.getName(), request));
    }
}
