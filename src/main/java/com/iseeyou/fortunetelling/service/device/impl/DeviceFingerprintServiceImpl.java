package com.iseeyou.fortunetelling.service.device.impl;

import com.iseeyou.fortunetelling.service.device.DeviceFingerprintService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceFingerprintServiceImpl implements DeviceFingerprintService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String TRUSTED_DEVICE_PREFIX = "trusted:device:";
    private static final long TRUSTED_DEVICE_TTL_DAYS = 90; // 3 months

    @Override
    public String generateFingerprint(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String ipAddress = getIpAddress(request);

        // Simple fingerprint: hash of (IP + User-Agent)
        String rawFingerprint = ipAddress + "|" + (userAgent != null ? userAgent : "unknown");

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawFingerprint.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to generate device fingerprint", e);
            return String.valueOf(rawFingerprint.hashCode());
        }
    }

    @Override
    public boolean isTrustedDevice(UUID userId, String fingerprint) {
        String key = TRUSTED_DEVICE_PREFIX + userId + ":" + fingerprint;
        return redisTemplate.hasKey(key);
    }

    @Override
    public void trustDevice(UUID userId, String fingerprint) {
        String key = TRUSTED_DEVICE_PREFIX + userId + ":" + fingerprint;
        redisTemplate.opsForValue().set(key, true, TRUSTED_DEVICE_TTL_DAYS, TimeUnit.DAYS);
        log.info("Trusted device {} for user {}", fingerprint, userId);
    }

    @Override
    public String getDeviceInfo(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown Device";
        }

        // Parse User-Agent to extract browser and OS info
        // Simple parsing - can be enhanced with library like UAParser
        if (userAgent.contains("Mobile")) {
            if (userAgent.contains("Android")) {
                return "Android Mobile";
            } else if (userAgent.contains("iPhone") || userAgent.contains("iPad")) {
                return "iOS Mobile";
            }
            return "Mobile Device";
        } else {
            if (userAgent.contains("Windows")) {
                return "Windows Desktop";
            } else if (userAgent.contains("Mac")) {
                return "Mac Desktop";
            } else if (userAgent.contains("Linux")) {
                return "Linux Desktop";
            }
            return "Desktop Device";
        }
    }

    @Override
    public String getIpAddress(HttpServletRequest request) {
        // Check for IP behind proxy/load balancer
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("X-Real-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        // X-Forwarded-For can contain multiple IPs, get the first one
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }

        return ipAddress;
    }
}
