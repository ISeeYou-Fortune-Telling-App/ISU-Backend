package com.iseeyou.fortunetelling.repository.chat;

import com.iseeyou.fortunetelling.entity.chat.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    Page<Message> findByConversation_IdOrderByCreatedAtDesc(UUID conversationId, Pageable pageable);
    List<Message> findByConversationIdAndIsReadFalse(UUID conversationId);
}
