package com.orginsight.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.orginsight.dto.request.ForgotPasswordRequest;
import com.orginsight.dto.request.LoginRequest;
import com.orginsight.dto.request.RegisterRequest;
import com.orginsight.dto.request.ResetPasswordRequest;
import com.orginsight.dto.response.LoginResponse;
import com.orginsight.dto.response.TokenResponse;
import com.orginsight.entity.PasswordResetToken;
import com.orginsight.entity.RefreshToken;
import com.orginsight.entity.Role;
import com.orginsight.entity.User;
import com.orginsight.exception.InvalidCredentialsException;
import com.orginsight.repository.PasswordResetTokenRepository;
import com.orginsight.repository.RefreshTokenRepository;
import com.orginsight.repository.UserRepository;
import com.orginsight.security.JwtService;
import com.orginsight.service.AuditLogService;
import com.orginsight.service.AuthenticationService;

@Service
@Transactional
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationServiceImpl.class);
    private static final long RESET_TOKEN_VALIDITY_MINUTES = 30;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final AuditLogService auditLogService;

    public AuthenticationServiceImpl(UserRepository userRepository,
                                     PasswordEncoder passwordEncoder,
                                     JwtService jwtService,
                                     AuthenticationManager authenticationManager,
                                     RefreshTokenRepository refreshTokenRepository,
                                     PasswordResetTokenRepository passwordResetTokenRepository,
                                     AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    public String register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("This username is already taken.");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() != null ? request.getRole() : Role.EMPLOYEE);
        user.setFullName(request.getUsername());
        userRepository.save(user);
        auditLogService.log(request.getEmail(), "USER_REGISTERED", "User", null, null);
        logger.info("User registered: {}", request.getEmail());
        return "User registered successfully";
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()));
        } catch (org.springframework.security.authentication.DisabledException e) {
            logger.warn("Login attempt for deactivated account: {}", request.getEmail());
            throw new InvalidCredentialsException("This account has been deactivated. Please contact an administrator.");
        } catch (AuthenticationException e) {
            logger.warn("Failed login attempt for: {}", request.getEmail());
            throw new BadCredentialsException("Incorrect email or password");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Incorrect email or password"));

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String accessToken = jwtService.generateToken(user.getEmail());
        String refreshToken = issueRefreshToken(user);

        logger.info("Successful login for: {}", user.getEmail());
        auditLogService.log(user.getEmail(), "LOGIN", "User", String.valueOf(user.getId()), null);
        return LoginResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtService.getAccessTokenExpirySeconds())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    @Override
    public void logout(String email) {
        userRepository.findByEmail(email).ifPresent(refreshTokenRepository::revokeAllForUser);
        auditLogService.log(email, "LOGOUT", "User", null, null);
        logger.info("User logged out and refresh tokens revoked: {}", email);
    }

    @Override
    public TokenResponse refreshToken(String refreshToken) {
        if (!jwtService.isTokenValid(refreshToken) || !jwtService.isRefreshToken(refreshToken)) {
            throw new InvalidCredentialsException("Refresh token is invalid or expired.");
        }

        RefreshToken stored = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new InvalidCredentialsException("Refresh token is invalid or expired."));

        if (stored.isRevoked() || stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidCredentialsException("Refresh token is invalid or expired.");
        }

        User user = stored.getUser();
        String newAccessToken = jwtService.generateToken(user.getEmail());

        return TokenResponse.builder()
                .token(newAccessToken)
                .refreshToken(stored.getToken())
                .expiresIn(jwtService.getAccessTokenExpirySeconds())
                .build();
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        // Always behave the same way regardless of whether the email exists,
        // to avoid leaking which emails are registered.
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token(token)
                    .user(user)
                    .expiresAt(LocalDateTime.now().plusMinutes(RESET_TOKEN_VALIDITY_MINUTES))
                    .build();
            passwordResetTokenRepository.save(resetToken);

            // NOTE: No SMTP/email provider is configured in this project yet.
            // Logging the reset link here as a development-mode stand-in;
            // wire this to a real mail sender (e.g. Spring Mail + SMTP or a
            // transactional email API) before going to production.
            logger.info("Password reset requested for {}. Reset token (send via email in production): {}",
                    user.getEmail(), token);
        });
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new InvalidCredentialsException("Reset token is invalid or has expired."));

        if (resetToken.isUsed() || resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidCredentialsException("Reset token is invalid or has expired.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        refreshTokenRepository.revokeAllForUser(user);
        auditLogService.log(user.getEmail(), "PASSWORD_RESET", "User", String.valueOf(user.getId()), null);
        logger.info("Password reset completed for {}", user.getEmail());
    }

    private String issueRefreshToken(User user) {
        String tokenValue = jwtService.generateRefreshToken(user.getEmail());
        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenValue)
                .user(user)
                .expiresAt(LocalDateTime.now().plusSeconds(jwtService.getRefreshTokenExpiryMillis() / 1000))
                .build();
        refreshTokenRepository.save(refreshToken);
        return tokenValue;
    }
}
