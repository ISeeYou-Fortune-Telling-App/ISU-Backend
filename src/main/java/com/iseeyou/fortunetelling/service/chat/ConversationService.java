package com.iseeyou.fortunetelling.service.chat;

import com.iseeyou.fortunetelling.dto.response.chat.session.ConversationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ConversationService {
    ConversationResponse createChatSession(UUID bookingId);

    // Admin creates conversation with any user (customer or seer)
    ConversationResponse createAdminConversation(UUID targetUserId, String initialMessage);

    ConversationResponse getConversation(UUID conversationId);
    ConversationResponse getChatSessionByBookingId(UUID bookingId);
    Page<ConversationResponse> getMyChatSessions(Pageable pageable);
    void endChatSession(UUID conversationId);

    // Auto-cancel late sessions
    void cancelLateSession(UUID conversationId);

    // Activate WAITING conversation when session_start_time arrives
    void activateWaitingConversation(UUID conversationId);

    // Warning & auto-end
    void sendWarningNotification(UUID conversationId);
    void autoEndSession(UUID conversationId);

    // Extend session
    void extendSession(UUID conversationId, Integer additionalMinutes);
}
