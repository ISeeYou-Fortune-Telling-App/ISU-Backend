package com.iseeyou.fortunetelling.service.auth;

import com.iseeyou.fortunetelling.dto.response.auth.TokenResponse;
import com.iseeyou.fortunetelling.entity.user.User;

import java.util.UUID;

public interface AuthService {
    TokenResponse login(String email, String password, String fcmToken, Boolean rememberMe);

    TokenResponse refreshFromBearerString(String bearer);

    void logout(User user, String bearer);

    void logout(User user);

    TokenResponse refresh(String refreshToken);

    TokenResponse generateTokens(UUID id, Boolean rememberMe);
}