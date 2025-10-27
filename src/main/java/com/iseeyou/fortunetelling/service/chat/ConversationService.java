package com.iseeyou.fortunetelling.service.chat;

import com.iseeyou.fortunetelling.dto.response.chat.ConversationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ConversationService {
    ConversationResponse createChatSession(UUID bookingId);
    ConversationResponse getConversation(UUID conversationId);
    ConversationResponse getChatSessionByBookingId(UUID bookingId);
    Page<ConversationResponse> getMyChatSessions(Pageable pageable);
    void endChatSession(UUID conversationId);

    // Auto-cancel late sessions
    void cancelLateSession(UUID conversationId);

    // Warning & auto-end
    void sendWarningNotification(UUID conversationId);
    void autoEndSession(UUID conversationId);

    // Extend session
    void extendSession(UUID conversationId, Integer additionalMinutes);
}
