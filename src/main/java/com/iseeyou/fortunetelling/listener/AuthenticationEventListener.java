package com.iseeyou.fortunetelling.listener;

import com.iseeyou.fortunetelling.dto.request.notification.NotificationCreateRequest;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.repository.user.UserRepository;
import com.iseeyou.fortunetelling.service.device.DeviceFingerprintService;
import com.iseeyou.fortunetelling.service.email.EmailVerificationService;
import com.iseeyou.fortunetelling.service.notification.NotificationService;
import com.iseeyou.fortunetelling.service.UserLoginHistoryService;
import com.iseeyou.fortunetelling.util.Constants;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import com.iseeyou.fortunetelling.service.event.LogoutSuccessEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationEventListener {

    private final NotificationService notificationService;
    private final EmailVerificationService emailVerificationService;
    private final DeviceFingerprintService deviceFingerprintService;
    private final UserLoginHistoryService loginHistoryService;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String FAILED_LOGIN_PREFIX = "failed:login:";
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int FAILED_LOGIN_WINDOW_MINUTES = 15;

    @EventListener
    public void onLoginSuccess(AuthenticationSuccessEvent event) {
        try {
            Authentication authentication = event.getAuthentication();
            HttpServletRequest request = getCurrentHttpRequest();

            if (request == null) {
                log.warn("Cannot get HttpServletRequest for login event");
                return;
            }

            // Get user info
            String email = authentication.getName();
            User user = userRepository.findByEmail(email).orElse(null);

            if (user == null) {
                log.warn("User not found for email: {}", email);
                return;
            }

            // Extract device info
            String ipAddress = deviceFingerprintService.getIpAddress(request);
            String deviceInfo = deviceFingerprintService.getDeviceInfo(request);
            String fingerprint = deviceFingerprintService.generateFingerprint(request);
            String location = getLocationFromIp(ipAddress);

            // Check if trusted device
            boolean isTrustedDevice = deviceFingerprintService.isTrustedDevice(user.getId(), fingerprint);

            // Save login history
            loginHistoryService.saveLoginHistory(
                    user.getId(),
                    ipAddress,
                    deviceInfo,
                    location,
                    fingerprint,
                    isTrustedDevice
            );

            LocalDateTime loginTime = LocalDateTime.now();

            if (isTrustedDevice) {
                // Trusted device - Send normal login notification
                NotificationCreateRequest notifRequest = NotificationCreateRequest.builder()
                        .notificationType(Constants.NotificationTypeEnum.ACCOUNT)
                        .notificationTitle("Đăng nhập thành công")
                        .notificationBody(String.format(
                                "Đăng nhập từ %s lúc %s",
                                deviceInfo,
                                loginTime.toString()
                        ))
                        .recipientId(user.getId())
                        .build();

                notificationService.createNotification(notifRequest);
                log.info("Created login notification for user {} from trusted device", user.getId());

            } else {
                // New device - Send security alert
                NotificationCreateRequest notifRequest = NotificationCreateRequest.builder()
                        .notificationType(Constants.NotificationTypeEnum.ACCOUNT)
                        .notificationTitle("!! Đăng nhập từ thiết bị mới")
                        .notificationBody(String.format(
                                "Phát hiện đăng nhập từ thiết bị mới: %s (IP: %s) lúc %s. " +
                                        "Nếu không phải bạn, vui lòng thay đổi mật khẩu ngay.",
                                deviceInfo,
                                ipAddress,
                                loginTime.toString()
                        ))
                        .recipientId(user.getId())
                        .build();

                notificationService.createNotification(notifRequest);

                // Send email alert for new device
                emailVerificationService.sendNewDeviceAlertEmail(
                        user.getId(),
                        ipAddress,
                        deviceInfo,
                        location,
                        loginTime
                );

                // Trust this device for future logins
                deviceFingerprintService.trustDevice(user.getId(), fingerprint);

                log.info("Created new device alert for user {}", user.getId());
            }

            // Clear failed login attempts counter
            clearFailedLoginAttempts(email);

        } catch (Exception e) {
            log.error("Error handling login success event", e);
        }
    }

    @EventListener
    public void onLoginFailure(AbstractAuthenticationFailureEvent event) {
        try {
            String email = event.getAuthentication().getName();
            HttpServletRequest request = getCurrentHttpRequest();

            if (request == null) {
                return;
            }

            String ipAddress = deviceFingerprintService.getIpAddress(request);
            String deviceInfo = deviceFingerprintService.getDeviceInfo(request);
            String failureReason = event.getException().getMessage();

            // Save failed login attempt
            loginHistoryService.saveFailedLoginAttempt(email, ipAddress, deviceInfo, failureReason);

            // Increment failed attempts counter
            int failedAttempts = incrementFailedLoginAttempts(email);

            log.warn("Failed login attempt for email: {} (attempt #{}) from IP: {}",
                    email, failedAttempts, ipAddress);

            // Send security alert if too many failed attempts
            if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
                User user = userRepository.findByEmail(email).orElse(null);

                if (user != null) {
                    NotificationCreateRequest notifRequest = NotificationCreateRequest.builder()
                            .notificationType(Constants.NotificationTypeEnum.ACCOUNT)
                            .notificationTitle("!! Cảnh báo bảo mật")
                            .notificationBody(String.format(
                                    "Phát hiện %d lần đăng nhập thất bại từ IP %s trong %d phút qua. " +
                                            "Nếu không phải bạn, vui lòng thay đổi mật khẩu ngay lập tức.",
                                    failedAttempts,
                                    ipAddress,
                                    FAILED_LOGIN_WINDOW_MINUTES
                            ))
                            .recipientId(user.getId())
                            .build();

                    notificationService.createNotification(notifRequest);

                    // Send email alert
                    emailVerificationService.sendSecurityAlertEmail(
                            user.getId(),
                            String.format("%d lần đăng nhập thất bại từ IP %s", failedAttempts, ipAddress),
                            "HIGH"
                    );

                    log.warn("Sent security alert to user {} due to {} failed login attempts",
                            user.getId(), failedAttempts);
                }
            }

        } catch (Exception e) {
            log.error("Error handling login failure event", e);
        }
    }

    @EventListener
    public void onLogout(LogoutSuccessEvent event) {
        try {
            Authentication authentication = event.getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return;
            }

            String email = authentication.getName();
            User user = userRepository.findByEmail(email).orElse(null);

            if (user == null) {
                return;
            }

            HttpServletRequest request = getCurrentHttpRequest();
            String deviceInfo = request != null ?
                    deviceFingerprintService.getDeviceInfo(request) : "Unknown Device";

            LocalDateTime logoutTime = LocalDateTime.now();

            // Create logout notification
            NotificationCreateRequest notifRequest = NotificationCreateRequest.builder()
                    .notificationType(Constants.NotificationTypeEnum.ACCOUNT)
                    .notificationTitle("Đăng xuất thành công")
                    .notificationBody(String.format(
                            "Đăng xuất từ %s lúc %s",
                            deviceInfo,
                            logoutTime.toString()
                    ))
                    .recipientId(user.getId())
                    .build();

            notificationService.createNotification(notifRequest);
            log.info("Created logout notification for user {}", user.getId());

        } catch (Exception e) {
            log.error("Error handling logout event", e);
        }
    }

    // Helper methods

    private HttpServletRequest getCurrentHttpRequest() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return attributes.getRequest();
        } catch (IllegalStateException e) {
            log.debug("No current HttpServletRequest available");
            return null;
        }
    }

    private String getLocationFromIp(String ipAddress) {
        // TODO: Implement IP geolocation using service
        // For now, just return null
        return null;
    }

    private int incrementFailedLoginAttempts(String email) {
        String key = FAILED_LOGIN_PREFIX + email;
        Long attempts = redisTemplate.opsForValue().increment(key);

        if (attempts == null) {
            attempts = 1L;
        }

        // Set expiration on first attempt
        if (attempts == 1) {
            redisTemplate.expire(key, FAILED_LOGIN_WINDOW_MINUTES, TimeUnit.MINUTES);
        }

        return attempts.intValue();
    }

    private void clearFailedLoginAttempts(String email) {
        String key = FAILED_LOGIN_PREFIX + email;
        redisTemplate.delete(key);
    }
}
