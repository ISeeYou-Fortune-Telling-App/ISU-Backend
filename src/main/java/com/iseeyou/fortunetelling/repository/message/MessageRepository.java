package com.iseeyou.fortunetelling.repository.message;

import com.iseeyou.fortunetelling.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    Page<Message> findByConversation_IdOrderByCreatedAtDesc(UUID conversationId, Pageable pageable);
    List<Message> findByConversation_Id(UUID conversationId);

    // Find messages that are not deleted by specific user
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId " +
            "AND :userId NOT MEMBER OF m.deletedByUserIds " +
            "ORDER BY m.createdAt DESC")
    Page<Message> findDeletedMessagesByUser(
            @Param("conversationId") UUID conversationId,
            @Param("userId") UUID userId,
            Pageable pageable
    );

    // Find messages by IDs and verify conversation
    @Query("SELECT m FROM Message m WHERE m.id IN :messageIds " +
            "AND m.conversation.id = :conversationId")
    List<Message> findByIdsAndConversation(
            @Param("messageIds") List<UUID> messageIds,
            @Param("conversationId") UUID conversationId
    );

    // Find deleted messages by user (for undo)
    @Query("SELECT m FROM Message m WHERE m.id IN :messageIds " +
            "AND :userId MEMBER OF m.deletedByUserIds " +
            "AND m.conversation.id = :conversationId")
    List<Message> findDeletedMessagesByUser(
            @Param("messageIds") List<UUID> messageIds,
            @Param("userId") UUID userId,
            @Param("conversationId") UUID conversationId
    );

    // Find visible messages (not recalled, not deleted by user)
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId " +
            "AND m.isRecalled = false " +
            "AND :userId NOT MEMBER OF m.deletedByUserIds " +
            "ORDER BY m.createdAt DESC")
    Page<Message> findVisibleMessagesByConversationAndUser(
            @Param("conversationId") UUID conversationId,
            @Param("userId") UUID userId,
            Pageable pageable
    );

    // Find messages sent by user (for recall validation)
    @Query("SELECT m FROM Message m WHERE m.id IN :messageIds " +
            "AND m.conversation.id = :conversationId " +
            "AND m.sender.id = :senderId " +
            "AND m.isRecalled = false")
    List<Message> findRecallableMessages(
            @Param("messageIds") List<UUID> messageIds,
            @Param("conversationId") UUID conversationId,
            @Param("senderId") UUID senderId
    );

    // Check if message can be recalled (within time limit)
    @Query("SELECT m FROM Message m WHERE m.id IN :messageIds " +
            "AND m.sender.id = :senderId " +
            "AND m.createdAt > :timeLimit " +
            "AND m.isRecalled = false")
    List<Message> findMessagesWithinRecallLimit(
            @Param("messageIds") List<UUID> messageIds,
            @Param("senderId") UUID senderId,
            @Param("timeLimit") LocalDateTime timeLimit
    );
}
