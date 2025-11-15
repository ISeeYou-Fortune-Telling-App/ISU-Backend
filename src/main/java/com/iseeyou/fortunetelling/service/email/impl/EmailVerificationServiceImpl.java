package com.iseeyou.fortunetelling.service.email.impl;

import com.iseeyou.fortunetelling.entity.EmailVerification;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.exception.NotFoundException;
import com.iseeyou.fortunetelling.repository.email.EmailVerificationRepository;
import com.iseeyou.fortunetelling.repository.user.UserRepository;
import com.iseeyou.fortunetelling.service.email.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

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
    @Transactional(propagation = Propagation.REQUIRES_NEW)
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
    @Transactional(propagation = Propagation.REQUIRES_NEW)
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

    @Override
    public void sendSeerWelcomeEmail(UUID userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("User not found"));

            String subject = appName + " - 🎉 Chào mừng bạn đến với nền tảng " + appName;
            String content = String.format(
                    "Xin chào %s!\n\n" +
                            "🎉 Chào mừng bạn đã trở thành một phần của cộng đồng Thầy/Cô tại %s!\n\n" +
                            "Cảm ơn bạn đã hoàn tất quá trình đăng ký và xác thực email. Chúng tôi rất vui mừng được chào đón bạn!\n\n" +
                            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                            "📋 HƯỚNG DẪN SỬ DỤNG NỀN TẢNG:\n\n" +
                            "1. Hoàn thiện hồ sơ: Cập nhật đầy đủ thông tin cá nhân, chuyên môn và chứng chỉ của bạn\n" +
                            "2. Thiết lập lịch làm việc: Cấu hình thời gian rảnh để khách hàng có thể đặt lịch\n" +
                            "3. Thiết lập gói dịch vụ: Tạo các gói tư vấn phù hợp với chuyên môn của bạn\n" +
                            "4. Quản lý booking: Theo dõi và xử lý các yêu cầu tư vấn từ khách hàng\n" +
                            "5. Chat với khách hàng: Sử dụng tính năng chat để tư vấn trực tuyến\n" +
                            "6. Quản lý thu nhập: Theo dõi doanh thu và rút tiền qua PayPal\n\n" +
                            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                            "📞 THÔNG TIN LIÊN HỆ HỖ TRỢ:\n\n" +
                            "- Email hỗ trợ: admin@gmail.com\n" +
                            "- Hotline: 1900-xxxx (8:00 - 22:00 hàng ngày)\n" +
                            "- Chat trực tiếp: Sử dụng tính năng chat với Admin trong ứng dụng\n\n" +
                            "Nếu bạn có bất kỳ thắc mắc nào, đừng ngần ngại liên hệ với chúng tôi. " +
                            "Đội ngũ hỗ trợ luôn sẵn sàng giúp đỡ bạn!\n\n" +
                            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                            "⏳ QUÁ TRÌNH DUYỆT HỒ SƠ:\n\n" +
                            "Hồ sơ của bạn đang được đội ngũ của chúng tôi xem xét kỹ lưỡng. " +
                            "Quá trình này thường mất từ 2-3 ngày làm việc.\n\n" +
                            "Trong thời gian này, vui lòng:\n" +
                            "✓ Đảm bảo các thông tin và chứng chỉ đã được cung cấp đầy đủ\n" +
                            "✓ Kiểm tra email thường xuyên để nhận thông báo\n" +
                            "✓ Chuẩn bị sẵn sàng để bắt đầu tư vấn sau khi được phê duyệt\n\n" +
                            "Chúng tôi sẽ thông báo qua email ngay khi quá trình duyệt hoàn tất.\n\n" +
                            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                            "💡 MẸO NHỎ:\n" +
                            "- Hồ sơ chi tiết và chuyên nghiệp sẽ được ưu tiên duyệt nhanh hơn\n" +
                            "- Chứng chỉ rõ ràng, hợp lệ sẽ tăng độ tin cậy với khách hàng\n" +
                            "- Cập nhật thường xuyên lịch làm việc để nhận nhiều booking hơn\n\n" +
                            "Cảm ơn bạn đã lựa chọn %s. Chúc bạn thành công và phát triển cùng nền tảng!\n\n" +
                            "Trân trọng,\n" +
                            "ISU Team\n" +
                            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
                    user.getFullName(),
                    appName,
                    fromEmail,
                    appName,
                    appName
            );

            sendEmail(user.getEmail(), subject, content);
            log.info("Sent welcome email to seer: {}", userId);

        } catch (Exception e) {
            log.error("Failed to send welcome email to seer {}", userId, e);
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
