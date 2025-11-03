package com.iseeyou.fortunetelling.service.chat;

import com.iseeyou.fortunetelling.dto.response.chat.session.ConversationResponse;
import com.iseeyou.fortunetelling.dto.response.chat.session.ConversationStatisticResponse;
import com.iseeyou.fortunetelling.util.Constants;
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
    Page<ConversationResponse> getAllChatSessionsWithFilters(
            Pageable pageable,
            String participantName,
            Constants.ConversationTypeEnum typeEnum,
            Constants.ConversationStatusEnum status
    );

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

    ConversationStatisticResponse getConversationStatistics();

    // Fix admin chat conversations that were incorrectly cancelled
    int fixAdminChatConversations();
}
