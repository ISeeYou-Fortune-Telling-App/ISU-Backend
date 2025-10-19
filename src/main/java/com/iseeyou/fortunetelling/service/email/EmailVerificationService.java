package com.iseeyou.fortunetelling.service.email;

public interface EmailVerificationService {

    void sendVerificationEmail(String email);

    void sendPasswordResetEmail(String email);

    boolean verifyOtp(String email, String otpCode);

    void cleanupExpiredOtps();
}
