package com.orginsight.service;

import com.orginsight.dto.request.ChangePasswordRequest;
import com.orginsight.dto.request.NotificationSettingsRequest;
import com.orginsight.dto.request.ProfileUpdateRequest;
import com.orginsight.dto.request.SecuritySettingsRequest;
import com.orginsight.dto.request.ThemeSettingsRequest;
import com.orginsight.dto.response.UserProfileResponse;

public interface SettingsService {
    UserProfileResponse getProfile(String currentUserEmail);
    UserProfileResponse updateProfile(String currentUserEmail, ProfileUpdateRequest request);
    void changePassword(String currentUserEmail, ChangePasswordRequest request);
    UserProfileResponse updateNotificationSettings(String currentUserEmail, NotificationSettingsRequest request);
    UserProfileResponse updateThemeSettings(String currentUserEmail, ThemeSettingsRequest request);
    UserProfileResponse updateSecuritySettings(String currentUserEmail, SecuritySettingsRequest request);
}
