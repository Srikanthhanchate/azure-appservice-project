package com.orginsight.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // ---- Profile fields (Settings module) ----
    @Column(name = "full_name", length = 150)
    private String fullName;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "job_title", length = 100)
    private String jobTitle;

    // ---- Notification settings ----
    @Builder.Default
    @Column(name = "email_notifications")
    private Boolean emailNotifications = true;

    @Builder.Default
    @Column(name = "push_notifications")
    private Boolean pushNotifications = true;

    @Builder.Default
    @Column(name = "weekly_digest")
    private Boolean weeklyDigest = true;

    // ---- Theme settings ----
    @Builder.Default
    @Column(name = "theme", length = 20)
    private String theme = "light";

    // ---- Security settings ----
    @Builder.Default
    @Column(name = "two_factor_enabled")
    private Boolean twoFactorEnabled = false;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @jakarta.persistence.PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
