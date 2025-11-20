package com.iseeyou.fortunetelling.service.email;

import java.time.LocalDateTime;
import java.util.UUID;

public interface EmailVerificationService {

    void sendVerificationEmail(String email);

    void sendPasswordResetEmail(String email);

    boolean verifyOtp(String email, String otpCode);

    void cleanupExpiredOtps();

    void sendLoginAlertEmail(UUID userId, String ipAddress, String deviceInfo, String location, LocalDateTime loginTime);
    void sendNewDeviceAlertEmail(UUID userId, String ipAddress, String deviceInfo, String location, LocalDateTime loginTime);
    void sendLogoutAlertEmail(UUID userId, String deviceInfo, LocalDateTime logoutTime);
    void sendSecurityAlertEmail(UUID userId, String alertMessage, String severity);

    void sendSeerWelcomeEmail(UUID userId);
}
