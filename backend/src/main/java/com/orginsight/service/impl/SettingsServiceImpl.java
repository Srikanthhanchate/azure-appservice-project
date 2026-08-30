package com.orginsight.service.impl;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.orginsight.dto.request.ChangePasswordRequest;
import com.orginsight.dto.request.NotificationSettingsRequest;
import com.orginsight.dto.request.ProfileUpdateRequest;
import com.orginsight.dto.request.SecuritySettingsRequest;
import com.orginsight.dto.request.ThemeSettingsRequest;
import com.orginsight.dto.response.UserProfileResponse;
import com.orginsight.entity.User;
import com.orginsight.exception.DuplicateResourceException;
import com.orginsight.exception.InvalidCredentialsException;
import com.orginsight.repository.UserRepository;
import com.orginsight.service.SettingsService;

@Service
@Transactional
public class SettingsServiceImpl implements SettingsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SettingsServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(String currentUserEmail) {
        return mapToResponse(findUser(currentUserEmail));
    }

    @Override
    public UserProfileResponse updateProfile(String currentUserEmail, ProfileUpdateRequest request) {
        User user = findUser(currentUserEmail);

        if (!user.getEmail().equalsIgnoreCase(request.getEmail())
                && userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateResourceException("An account with this email already exists.");
        }

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setJobTitle(request.getJobTitle());
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        return mapToResponse(userRepository.save(user));
    }

    @Override
    public void changePassword(String currentUserEmail, ChangePasswordRequest request) {
        User user = findUser(currentUserEmail);
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect.");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public UserProfileResponse updateNotificationSettings(String currentUserEmail, NotificationSettingsRequest request) {
        User user = findUser(currentUserEmail);
        if (request.getEmailNotifications() != null) {
            user.setEmailNotifications(request.getEmailNotifications());
        }
        if (request.getPushNotifications() != null) {
            user.setPushNotifications(request.getPushNotifications());
        }
        if (request.getWeeklyDigest() != null) {
            user.setWeeklyDigest(request.getWeeklyDigest());
        }
        return mapToResponse(userRepository.save(user));
    }

    @Override
    public UserProfileResponse updateThemeSettings(String currentUserEmail, ThemeSettingsRequest request) {
        User user = findUser(currentUserEmail);
        if (request.getTheme() != null) {
            user.setTheme(request.getTheme());
        }
        return mapToResponse(userRepository.save(user));
    }

    @Override
    public UserProfileResponse updateSecuritySettings(String currentUserEmail, SecuritySettingsRequest request) {
        User user = findUser(currentUserEmail);
        if (request.getTwoFactorEnabled() != null) {
            user.setTwoFactorEnabled(request.getTwoFactorEnabled());
        }
        return mapToResponse(userRepository.save(user));
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private UserProfileResponse mapToResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .jobTitle(user.getJobTitle())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole() == null ? null : user.getRole().name())
                .emailNotifications(user.getEmailNotifications())
                .pushNotifications(user.getPushNotifications())
                .weeklyDigest(user.getWeeklyDigest())
                .theme(user.getTheme())
                .twoFactorEnabled(user.getTwoFactorEnabled())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
