package com.iseeyou.fortunetelling.repository.chat;

import com.iseeyou.fortunetelling.entity.chat.Message;
import com.iseeyou.fortunetelling.util.Constants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    Page<Message> findByConversation_IdOrderByCreatedAtDesc(UUID conversationId, Pageable pageable);
    List<Message> findByConversationIdAndStatus(UUID conversationId, Constants.MessageStatusEnum status);

    // Get messages excluding those deleted by specific role
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId " +
           "AND (m.deletedBy IS NULL OR m.deletedBy != :excludeRole) " +
           "ORDER BY m.createdAt DESC")
    Page<Message> findVisibleMessages(@Param("conversationId") UUID conversationId,
                                       @Param("excludeRole") Constants.RoleEnum excludeRole,
                                       Pageable pageable);
}
