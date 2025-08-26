package com.iseeyou.fortunetelling.service.auth;

import com.iseeyou.fortunetelling.entity.auth.JwtToken;

import java.util.UUID;

public interface JwtTokenService {
    JwtToken findByUserIdAndRefreshToken(UUID id, String refreshToken);

    JwtToken findByTokenOrRefreshToken(String token, String refreshToken);

    void save(JwtToken jwtToken);

    void delete(JwtToken jwtToken);
}