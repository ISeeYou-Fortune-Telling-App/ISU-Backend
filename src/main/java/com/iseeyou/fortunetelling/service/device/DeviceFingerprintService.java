package com.iseeyou.fortunetelling.service.device;

import jakarta.servlet.http.HttpServletRequest;

import java.util.UUID;

public interface DeviceFingerprintService {

    String generateFingerprint(HttpServletRequest request);
    //Check if device is trusted for this user
    boolean isTrustedDevice(UUID userId, String fingerprint);
    void trustDevice(UUID userId, String fingerprint);
    String getDeviceInfo(HttpServletRequest request);
    String getIpAddress(HttpServletRequest request);

}
