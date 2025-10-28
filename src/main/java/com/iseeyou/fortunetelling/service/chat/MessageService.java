package com.iseeyou.fortunetelling.service.chat;

import com.iseeyou.fortunetelling.dto.request.chat.session.ChatMessageRequest;
import com.iseeyou.fortunetelling.dto.response.chat.session.ChatMessageResponse;
import com.iseeyou.fortunetelling.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MessageService {
    ChatMessageResponse sendMessage(UUID conversationId, ChatMessageRequest request);
    ChatMessageResponse sendMessage(UUID conversationId, ChatMessageRequest request, User sender);
    Page<ChatMessageResponse> getMessages(UUID conversationId, Pageable pageable);
    void markMessagesAsRead(UUID conversationId);
    void deleteMessage(UUID messageId);
}
