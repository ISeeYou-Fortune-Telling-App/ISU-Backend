package com.iseeyou.fortunetelling.service.converstation;

import com.iseeyou.fortunetelling.dto.response.converstation.ChatSessionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ConverstationService {
    ChatSessionResponse createChatSession(UUID bookingId);
    ChatSessionResponse getChatSessionByBookingId(UUID bookingId);
    Page<ChatSessionResponse> getMyChatSessions(Pageable pageable);
    void endChatSession(UUID conversationId);
}
