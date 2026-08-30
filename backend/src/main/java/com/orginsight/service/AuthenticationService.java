package com.orginsight.service;

import com.orginsight.dto.request.ForgotPasswordRequest;
import com.orginsight.dto.request.LoginRequest;
import com.orginsight.dto.request.RegisterRequest;
import com.orginsight.dto.request.ResetPasswordRequest;
import com.orginsight.dto.response.LoginResponse;
import com.orginsight.dto.response.TokenResponse;

public interface AuthenticationService {
    String register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
    void logout(String email);
    TokenResponse refreshToken(String refreshToken);
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
}
