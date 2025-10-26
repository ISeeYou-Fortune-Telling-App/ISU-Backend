package com.iseeyou.fortunetelling.service;

import com.iseeyou.fortunetelling.entity.UserLoginHistory;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.exception.NotFoundException;
import com.iseeyou.fortunetelling.repository.UserLoginHistoryRepository;
import com.iseeyou.fortunetelling.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserLoginHistoryService {

    private final UserLoginHistoryRepository loginHistoryRepository;
    private final UserRepository userRepository;

    @Transactional
    public void saveLoginHistory(UUID userId, String ipAddress, String deviceInfo,
                                 String location, String deviceFingerprint, Boolean isTrusted) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("User not found"));

            UserLoginHistory history = UserLoginHistory.builder()
                    .user(user)
                    .ipAddress(ipAddress)
                    .deviceInfo(deviceInfo)
                    .location(location)
                    .deviceFingerprint(deviceFingerprint)
                    .isTrustedDevice(isTrusted)
                    .loginTime(LocalDateTime.now())
                    .loginSuccess(true)
                    .build();

            loginHistoryRepository.save(history);
            log.info("Saved login history for user {}", userId);

        } catch (Exception e) {
            log.error("Failed to save login history for user {}", userId, e);
        }
    }

    @Transactional
    public void saveFailedLoginAttempt(String email, String ipAddress, String deviceInfo, String failureReason) {
        try {
            User user = userRepository.findByEmail(email).orElse(null);

            if (user != null) {
                UserLoginHistory history = UserLoginHistory.builder()
                        .user(user)
                        .ipAddress(ipAddress)
                        .deviceInfo(deviceInfo)
                        .loginTime(LocalDateTime.now())
                        .loginSuccess(false)
                        .failureReason(failureReason)
                        .build();

                loginHistoryRepository.save(history);
                log.info("Saved failed login attempt for user {}", email);
            }

        } catch (Exception e) {
            log.error("Failed to save failed login attempt for {}", email, e);
        }
    }

    @Transactional(readOnly = true)
    public Page<UserLoginHistory> getLoginHistoryByUser(UUID userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return loginHistoryRepository.findAllByUserOrderByLoginTimeDesc(user, pageable);
    }

    @Transactional(readOnly = true)
    public Long countRecentFailedAttempts(UUID userId, int minutesAgo) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        LocalDateTime since = LocalDateTime.now().minusMinutes(minutesAgo);
        return loginHistoryRepository.countFailedLoginAttempts(user, since);
    }
}
