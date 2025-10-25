package com.iseeyou.fortunetelling.service.converstation;

import com.iseeyou.fortunetelling.dto.request.converstation.ChatHistoryFilterRequest;
import com.iseeyou.fortunetelling.dto.response.converstation.ChatSessionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ConversationService {
    ChatSessionResponse createChatSession(UUID bookingId);
    ChatSessionResponse getChatSessionByBookingId(UUID bookingId);
    Page<ChatSessionResponse> getMyChatSessions(Pageable pageable);
    void endChatSession(UUID conversationId);

    // Auto-cancel late sessions
    void cancelLateSession(UUID conversationId);

    //Warning & auto-end
    void sendWarningNotification(UUID conversationId);
    void autoEndSession(UUID conversationId); // auto end if 10 mins late (system)

    //Extend session
    void extendSession(UUID conversationId, Integer additionalMinutes);

    Page<ChatSessionResponse> getChatHistory(ChatHistoryFilterRequest filter, Pageable pageable);
}
