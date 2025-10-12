package com.iseeyou.fortunetelling.repository;

import com.iseeyou.fortunetelling.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, UUID> {

    Optional<EmailVerification> findByEmailAndOtpCodeAndIsUsedFalse(String email, String otpCode);

    @Query("SELECT ev FROM EmailVerification ev WHERE ev.email = :email AND ev.isUsed = false AND ev.expiresAt > :now ORDER BY ev.createdAt DESC")
    Optional<EmailVerification> findLatestValidOtpByEmail(@Param("email") String email, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE EmailVerification ev SET ev.isUsed = true WHERE ev.email = :email AND ev.isUsed = false")
    void markAllOtpAsUsedByEmail(@Param("email") String email);

    @Modifying
    @Query("DELETE FROM EmailVerification ev WHERE ev.expiresAt < :now")
    void deleteExpiredOtps(@Param("now") LocalDateTime now);
}
