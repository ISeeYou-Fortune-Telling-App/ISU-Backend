package com.iseeyou.fortunetelling.service.email.impl;

import com.iseeyou.fortunetelling.entity.EmailVerification;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.exception.NotFoundException;
import com.iseeyou.fortunetelling.repository.EmailVerificationRepository;
import com.iseeyou.fortunetelling.repository.user.UserRepository;
import com.iseeyou.fortunetelling.service.email.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.name}")
    private String appName;

    private static final SecureRandom random = new SecureRandom();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Override
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

    @Override
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


    @Override
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


    @Override
    @Transactional
    public void cleanupExpiredOtps() {
        emailVerificationRepository.deleteExpiredOtps(LocalDateTime.now());
        log.info("Cleaned up expired OTPs");
    }

    @Override
    public void sendLoginAlertEmail(UUID userId, String ipAddress, String deviceInfo, String location, LocalDateTime loginTime) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("User not found"));

            String subject = appName + " - Thông báo đăng nhập";
            String content = String.format(
                    "Xin chào %s!\n\n" +
                            "Chúng tôi nhận thấy có một hoạt động đăng nhập vào tài khoản của bạn:\n\n" +
                            "Thời gian: %s\n" +
                            "Địa chỉ IP: %s\n" +
                            "Thiết bị: %s\n" +
                            "Vị trí: %s\n\n" +
                            "Nếu đây là bạn, bạn có thể bỏ qua email này.\n" +
                            "Nếu bạn không thực hiện hành động này, vui lòng thay đổi mật khẩu ngay lập tức và liên hệ với chúng tôi.\n\n" +
                            "Trân trọng,\n%s Team",
                    user.getFullName(),
                    loginTime.format(FORMATTER),
                    ipAddress,
                    deviceInfo,
                    location != null ? location : "Không xác định",
                    appName
            );

            sendEmail(user.getEmail(), subject, content);
            log.info("Sent login alert email to user {}", userId);

        } catch (Exception e) {
            log.error("Failed to send login alert email to user {}", userId, e);
        }
    }

    @Override
    public void sendNewDeviceAlertEmail(UUID userId, String ipAddress, String deviceInfo, String location, LocalDateTime loginTime) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("User not found"));

            String subject = appName + " - ⚠️ Cảnh báo đăng nhập từ thiết bị mới";
            String content = String.format(
                    "Xin chào %s!\n\n" +
                            "!! CẢNH BÁO BẢO MẬT !!\n\n" +
                            "Chúng tôi phát hiện đăng nhập từ một thiết bị MỚI vào tài khoản của bạn:\n\n" +
                            "Thời gian: %s\n" +
                            "Địa chỉ IP: %s\n" +
                            "Thiết bị: %s\n" +
                            "Vị trí: %s\n\n" +
                            "Nếu đây là bạn:\n" +
                            "- Bạn có thể bỏ qua email này\n" +
                            "- Thiết bị này sẽ được ghi nhớ cho các lần đăng nhập sau\n\n" +
                            "Nếu KHÔNG phải bạn:\n" +
                            "- Thay đổi mật khẩu NGAY LẬP TỨC\n" +
                            "- Kiểm tra các hoạt động gần đây trong tài khoản\n" +
                            "- Liên hệ với chúng tôi để được hỗ trợ\n\n" +
                            "Trân trọng,\n%s Team",
                    user.getFullName(),
                    loginTime.format(FORMATTER),
                    ipAddress,
                    deviceInfo,
                    location != null ? location : "Không xác định",
                    appName
            );

            sendEmail(user.getEmail(), subject, content);
            log.info("Sent new device alert email to user {}", userId);

        } catch (Exception e) {
            log.error("Failed to send new device alert email to user {}", userId, e);
        }
    }

    @Override
    public void sendLogoutAlertEmail(UUID userId, String deviceInfo, LocalDateTime logoutTime) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("User not found"));

            String subject = appName + " - Thông báo đăng xuất";
            String content = String.format(
                    "Xin chào %s!\n\n" +
                            "Tài khoản của bạn đã đăng xuất:\n\n" +
                            "Thời gian: %s\n" +
                            "Thiết bị: %s\n\n" +
                            "Nếu bạn không thực hiện hành động này, vui lòng liên hệ với chúng tôi ngay.\n\n" +
                            "Trân trọng,\n%s Team",
                    user.getFullName(),
                    logoutTime.format(FORMATTER),
                    deviceInfo,
                    appName
            );

            sendEmail(user.getEmail(), subject, content);
            log.info("Sent logout alert email to user {}", userId);

        } catch (Exception e) {
            log.error("Failed to send logout alert email to user {}", userId, e);
        }
    }

    @Override
    public void sendSecurityAlertEmail(UUID userId, String alertMessage, String severity) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("User not found"));

            String emoji = severity.equals("HIGH") ? "🚨" : "⚠️";
            String subject = appName + " - " + emoji + " Cảnh báo bảo mật " + severity;
            String content = String.format(
                    "Xin chào %s!\n\n" +
                            "%s CẢNH BÁO BẢO MẬT [%s] %s\n\n" +
                            "%s\n\n" +
                            "Khuyến nghị:\n" +
                            "- Thay đổi mật khẩu ngay lập tức\n" +
                            "- Kiểm tra các hoạt động gần đây\n" +
                            "- Đảm bảo không ai khác có quyền truy cập tài khoản của bạn\n" +
                            "- Liên hệ với chúng tôi nếu cần hỗ trợ\n\n" +
                            "Trân trọng,\n%s Team",
                    user.getFullName(),
                    emoji,
                    severity,
                    emoji,
                    alertMessage,
                    appName
            );

            sendEmail(user.getEmail(), subject, content);
            log.info("Sent security alert email ({}) to user {}", severity, userId);

        } catch (Exception e) {
            log.error("Failed to send security alert email to user {}", userId, e);
        }
    }


    private String generateOtp() {
        return String.format("%06d", random.nextInt(1000000));
    }


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
                    "Trân trọng,\n%s Team",
                    appName, otpCode, appName
            );
        } else {
            emailContent = String.format(
                    "Xin chào!\n\n" +
                    "Cảm ơn bạn đã đăng ký tài khoản tại %s.\n\n" +
                    "Mã xác thực của bạn là: %s\n\n" +
                    "Mã này sẽ hết hạn sau 5 phút.\n\n" +
                    "Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này.\n\n" +
                    "Trân trọng,\n%s Team",
                    appName, otpCode, appName
            );
        }

        message.setText(emailContent);
        mailSender.send(message);
    }

    private void sendEmail(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        mailSender.send(message);
    }
}
