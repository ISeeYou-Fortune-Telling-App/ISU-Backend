package com.iseeyou.fortunetelling.repository.chat;

import com.iseeyou.fortunetelling.entity.chat.Conversation;
import com.iseeyou.fortunetelling.util.Constants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    Optional<Conversation> findByBookingId(UUID bookingId);
    Page<Conversation> findByBooking_ServicePackage_Seer_Id(UUID seerId, Pageable pageable);
    Page<Conversation> findByBooking_Customer_Id(UUID customerId, Pageable pageable);

    // Find conversation with all related entities loaded (avoid lazy loading issues)
    @Query("SELECT c FROM Conversation c " +
           "LEFT JOIN FETCH c.booking b " +
           "LEFT JOIN FETCH b.customer " +
           "LEFT JOIN FETCH b.servicePackage sp " +
           "LEFT JOIN FETCH sp.seer " +
           "WHERE c.id = :conversationId")
    Optional<Conversation> findByIdWithDetails(@Param("conversationId") UUID conversationId);

    // Find late sessions (customer hoặc seer chưa join sau 10 phút)
    @Query("SELECT conv FROM Conversation conv WHERE " +
            "conv.status = :status AND " +
            "(conv.customerJoinedAt IS NULL OR conv.seerJoinedAt IS NULL) AND " +
            "conv.sessionStartTime < :cutoffTime")
    List<Conversation> findLateSessions(
            @Param("status") Constants.ConversationStatusEnum status,
            @Param("cutoffTime") LocalDateTime cutoffTime
    );

    // Find sessions need to warning (10 mins left)
    @Query("SELECT conv FROM Conversation conv WHERE " +
            "conv.status = :status AND " +
            "conv.warningNotificationSent = false AND " +
            "conv.sessionEndTime > :now AND " +
            "conv.sessionEndTime <= :warningTime")
    List<Conversation> findSessionsNeedingWarning(
            @Param("status") Constants.ConversationStatusEnum status,
            @Param("now") LocalDateTime now,
            @Param("warningTime") LocalDateTime warningTime
    );

    // Find expired sessions
    @Query("SELECT conv FROM Conversation conv WHERE " +
            "conv.status = :status AND " +
            "conv.sessionEndTime <= :now")
    List<Conversation> findExpiredSessions(
            @Param("status") Constants.ConversationStatusEnum status,
            @Param("now") LocalDateTime now
    );
}
