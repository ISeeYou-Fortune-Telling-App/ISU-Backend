package com.iseeyou.fortunetelling.repository;

import com.iseeyou.fortunetelling.entity.UserLoginHistory;
import com.iseeyou.fortunetelling.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface UserLoginHistoryRepository extends JpaRepository<UserLoginHistory, UUID> {
    Page<UserLoginHistory> findAllByUserOrderByLoginTimeDesc(User user, Pageable pageable);

    @Query("SELECT COUNT(h) FROM UserLoginHistory h WHERE h.user = :user " +
            "AND h.loginSuccess = false AND h.loginTime > :since")
    Long countFailedLoginAttempts(@Param("user") User user, @Param("since") LocalDateTime since);

    @Query("SELECT h FROM UserLoginHistory h WHERE h.user = :user " +
            "AND h.deviceFingerprint = :fingerprint AND h.isTrustedDevice = true " +
            "ORDER BY h.loginTime DESC")
    Page<UserLoginHistory> findTrustedDeviceHistory(
            @Param("user") User user,
            @Param("fingerprint") String fingerprint,
            Pageable pageable
    );
}
