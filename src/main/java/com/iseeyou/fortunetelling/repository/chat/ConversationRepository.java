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

    // Find late sessions (customer hoặc seer chưa join sau 10 phút từ session_start_time)
    @Query("SELECT conv FROM Conversation conv WHERE " +
            "conv.status = :status AND " +
            "(conv.customerJoinedAt IS NULL OR conv.seerJoinedAt IS NULL) AND " +
            "conv.sessionStartTime <= :cutoffTime")
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

    // Find WAITING conversations that should be activated
    @Query("SELECT conv FROM Conversation conv WHERE " +
            "conv.status = :waitingStatus AND " +
            "conv.sessionStartTime <= :now")
    List<Conversation> findWaitingConversationsToActivate(
            @Param("waitingStatus") Constants.ConversationStatusEnum waitingStatus,
            @Param("now") LocalDateTime now
    );

    // Find admin conversation between admin and target user
    @Query("SELECT c FROM Conversation c WHERE " +
            "c.type = 'ADMIN_CHAT' AND " +
            "c.admin.id = :adminId AND " +
            "c.targetUser.id = :targetUserId")
    Optional<Conversation> findAdminConversationByAdminAndTarget(
            @Param("adminId") UUID adminId,
            @Param("targetUserId") UUID targetUserId
    );

    // Find all admin conversations for admin
    @Query("SELECT c FROM Conversation c WHERE " +
            "c.type = 'ADMIN_CHAT' AND " +
            "c.admin.id = :adminId")
    Page<Conversation> findAdminConversationsByAdmin(
            @Param("adminId") UUID adminId,
            Pageable pageable
    );

    // Find all admin conversations for target user (customer or seer)
    @Query("SELECT c FROM Conversation c WHERE " +
            "c.type = 'ADMIN_CHAT' AND " +
            "c.targetUser.id = :targetUserId")
    Page<Conversation> findAdminConversationsByTargetUser(
            @Param("targetUserId") UUID targetUserId,
            Pageable pageable
    );

    // Find all conversations for a customer (booking conversations + admin conversations as target)
    @Query("SELECT c FROM Conversation c " +
            "LEFT JOIN c.booking b " +
            "WHERE (b.customer.id = :customerId) OR " +
            "(c.type = 'ADMIN_CHAT' AND c.targetUser.id = :customerId)")
    Page<Conversation> findAllConversationsByCustomer(
            @Param("customerId") UUID customerId,
            Pageable pageable
    );

    // Find all conversations for a seer (booking conversations + admin conversations as target)
    @Query("SELECT c FROM Conversation c " +
            "LEFT JOIN c.booking b " +
            "LEFT JOIN b.servicePackage sp " +
            "WHERE (sp.seer.id = :seerId) OR " +
            "(c.type = 'ADMIN_CHAT' AND c.targetUser.id = :seerId)")
    Page<Conversation> findAllConversationsBySeer(
            @Param("seerId") UUID seerId,
            Pageable pageable
    );
}
