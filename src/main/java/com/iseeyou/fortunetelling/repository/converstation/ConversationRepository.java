package com.iseeyou.fortunetelling.repository.converstation;

import com.iseeyou.fortunetelling.entity.Conversation;
import com.iseeyou.fortunetelling.entity.user.User;
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
    Page<Conversation> findByBooking_ServicePackage_Seer(UUID seerId, Pageable pageable);
    Page<Conversation> findByBooking_Customer(UUID customerId, Pageable pageable);

    // Find late sessions (customer chưa join sau 10 phút)
    @Query("SELECT conv FROM Conversation conv WHERE " +
            "conv.status = :status AND " +
            "conv.customerJoinedAt IS NULL AND " +
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

    // FOR ADMIN: Get all conversations with filters (participant name, conversation type, statuses)
    @Query("SELECT DISTINCT conv FROM Conversation conv " +
            "LEFT JOIN FETCH conv.booking b " +
            "LEFT JOIN FETCH b.customer c " +
            "LEFT JOIN FETCH b.servicePackage sp " +
            "LEFT JOIN FETCH sp.seer s " +
            "WHERE 1=1 " +
            "AND (:participantName IS NULL OR " +
            "     LOWER(c.fullName) LIKE LOWER(CONCAT('%', :participantName, '%')) OR " +
            "     LOWER(s.fullName) LIKE LOWER(CONCAT('%', :participantName, '%'))) " +
            "AND (:conversationType IS NULL OR conv.type = :conversationType) " +
            "AND (:statuses IS NULL OR conv.status IN :statuses) " +
            "ORDER BY COALESCE(conv.sessionEndTime, conv.createdAt) DESC")
    Page<Conversation> findChatHistoryWithFilters(
            @Param("participantName") String participantName,
            @Param("conversationType") Constants.ConversationTypeEnum conversationType,
            @Param("statuses") List<Constants.ConversationStatusEnum> statuses,
            Pageable pageable
    );

    // FOR NON-ADMIN: Search by MESSAGE CONTENT in their own conversations
    @Query("SELECT DISTINCT conv FROM Conversation conv " +
            "LEFT JOIN FETCH conv.booking b " +
            "LEFT JOIN FETCH b.customer c " +
            "LEFT JOIN FETCH b.servicePackage sp " +
            "LEFT JOIN FETCH sp.seer s " +
            "LEFT JOIN conv.messages msg " +
            "WHERE (c.id = :userId OR s.id = :userId) " +
            "AND (:keyword IS NULL OR :keyword = '' OR " +
            "     LOWER(msg.textContent) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND msg.isDeleted = false " +
            "AND msg.isRemoved = false " +
            "ORDER BY COALESCE(conv.sessionEndTime, conv.createdAt) DESC")
    Page<Conversation> searchUserConversationsByMessageContent(
            @Param("userId") UUID userId,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
