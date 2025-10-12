package com.iseeyou.fortunetelling.service.email;

import com.iseeyou.fortunetelling.entity.EmailVerification;
import com.iseeyou.fortunetelling.repository.EmailVerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.name}")
    private String appName;

    private static final SecureRandom random = new SecureRandom();

    /**
     * Gửi OTP xác thực email
     */
    @Transactional
    public void sendVerificationEmail(String email) {
        try {
            // Vô hiệu hóa tất cả OTP cũ của email này
            emailVerificationRepository.markAllOtpAsUsedByEmail(email);

            // Tạo OTP mới
            String otpCode = generateOtp();

            // Lưu OTP vào database
            EmailVerification verification = EmailVerification.builder()
                    .email(email)
                    .otpCode(otpCode)
                    .build();

            emailVerificationRepository.save(verification);

            // Gửi email
            sendOtpEmail(email, otpCode, "Xác thực email");

            log.info("Verification email sent successfully to: {}", email);

        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", email, e);
            throw new RuntimeException("Failed to send verification email");
        }
    }

    /**
     * Gửi OTP reset password
     */
    @Transactional
    public void sendPasswordResetEmail(String email) {
        try {
            // Vô hiệu hóa tất cả OTP cũ của email này
            emailVerificationRepository.markAllOtpAsUsedByEmail(email);

            // Tạo OTP mới
            String otpCode = generateOtp();

            // Lưu OTP vào database
            EmailVerification verification = EmailVerification.builder()
                    .email(email)
                    .otpCode(otpCode)
                    .build();

            emailVerificationRepository.save(verification);

            // Gửi email reset password
            sendOtpEmail(email, otpCode, "Đặt lại mật khẩu");

            log.info("Password reset email sent successfully to: {}", email);

        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", email, e);
            throw new RuntimeException("Failed to send password reset email");
        }
    }

    /**
     * Xác thực OTP
     */
    @Transactional
    public boolean verifyOtp(String email, String otpCode) {
        try {
            Optional<EmailVerification> verificationOpt = emailVerificationRepository
                    .findByEmailAndOtpCodeAndIsUsedFalse(email, otpCode);

            if (verificationOpt.isEmpty()) {
                log.warn("Invalid OTP attempted for email: {}", email);
                return false;
            }

            EmailVerification verification = verificationOpt.get();

            if (verification.isExpired()) {
                log.warn("Expired OTP attempted for email: {}", email);
                return false;
            }

            // Đánh dấu OTP đã được sử dụng
            verification.setUsed(true);
            emailVerificationRepository.save(verification);

            log.info("OTP verified successfully for email: {}", email);
            return true;
        } catch (Exception e) {
            log.error("Failed to verify OTP for email: {}", email, e);
            return false;
        }
    }

    /**
     * Tạo OTP 6 chữ số
     */
    private String generateOtp() {
        return String.format("%06d", random.nextInt(1000000));
    }

    /**
     * Gửi email chứa OTP
     */
    private void sendOtpEmail(String email, String otpCode, String subject) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject(appName + " - " + subject);

        String emailContent;
        if (subject.contains("Đặt lại mật khẩu")) {
            emailContent = String.format(
                    "Xin chào!\n\n" +
                    "Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản tại %s.\n\n" +
                    "Mã xác thực của bạn là: %s\n\n" +
                    "Mã này sẽ hết hạn sau 5 phút.\n\n" +
                    "Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này và mật khẩu của bạn sẽ không thay đổi.\n\n" +
                    "Trân trọng,\n",
                    appName, otpCode, appName
            );
        } else {
            emailContent = String.format(
                    "Xin chào!\n\n" +
                    "Cảm ơn bạn đã đăng ký tài khoản tại %s.\n\n" +
                    "Mã xác thực của bạn là: %s\n\n" +
                    "Mã này sẽ hết hạn sau 5 phút.\n\n" +
                    "Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này.\n\n" +
                    "Trân trọng,\n",
                    appName, otpCode, appName
            );
        }

        message.setText(emailContent);
        mailSender.send(message);
    }

    /**
     * Cleanup các OTP đã hết hạn
     */
    @Transactional
    public void cleanupExpiredOtps() {
        emailVerificationRepository.deleteExpiredOtps(LocalDateTime.now());
        log.info("Cleaned up expired OTPs");
    }
}
