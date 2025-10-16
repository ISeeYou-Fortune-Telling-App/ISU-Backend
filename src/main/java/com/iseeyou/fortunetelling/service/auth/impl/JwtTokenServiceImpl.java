package com.iseeyou.fortunetelling.service.auth.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.iseeyou.fortunetelling.entity.auth.JwtToken;
import com.iseeyou.fortunetelling.exception.NotFoundException;
import com.iseeyou.fortunetelling.repository.auth.JwtTokenRepository;
import com.iseeyou.fortunetelling.service.MessageSourceService;
import com.iseeyou.fortunetelling.service.auth.JwtTokenService;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtTokenServiceImpl implements JwtTokenService {
    private final JwtTokenRepository jwtTokenRepository;

    private final MessageSourceService messageSourceService;

    /**
     * Find a JWT token by user id and refresh token.
     *
     * @param id           UUID
     * @param refreshToken String
     * @return JwtToken
     */
    @Override
    public JwtToken findByUserIdAndRefreshToken(UUID id, String refreshToken) {
        return jwtTokenRepository.findByUserIdAndRefreshToken(id, refreshToken)
                .orElseThrow(() -> new NotFoundException(messageSourceService.get("not_found_with_param",
                        new String[]{messageSourceService.get("token")})));
    }

    /**
     * Find a JWT token by token or refresh token.
     *
     * @param token String
     * @param refreshToken String
     * @return JwtToken
     */
    @Override
    public JwtToken findByTokenOrRefreshToken(String token, String refreshToken) {
        Optional<JwtToken> jwtToken = jwtTokenRepository.findByTokenOrRefreshToken(token, refreshToken);
        if (jwtToken.isEmpty()) {
            throw new NotFoundException("Token not found");
        }
        return jwtToken.get();
    }

    /**
     * Save a JWT token.
     *
     * @param jwtToken JwtToken
     */
    @Override
    public void save(JwtToken jwtToken) {
        jwtTokenRepository.save(jwtToken);
    }

    /**
     * Delete a JWT token.
     *
     * @param jwtToken JwtToken
     */
    @Override
    public void delete(JwtToken jwtToken) {
        jwtTokenRepository.delete(jwtToken);
        log.info("Deleted token: {}", jwtToken);
    }
}