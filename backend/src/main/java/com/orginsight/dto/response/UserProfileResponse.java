package com.orginsight.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String jobTitle;
    private String avatarUrl;
    private String role;
    private Boolean emailNotifications;
    private Boolean pushNotifications;
    private Boolean weeklyDigest;
    private String theme;
    private Boolean twoFactorEnabled;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}
