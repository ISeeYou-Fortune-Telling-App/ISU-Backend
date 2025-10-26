package com.iseeyou.fortunetelling.service.message;

import com.iseeyou.fortunetelling.dto.request.message.ChatMessageRequest;
import com.iseeyou.fortunetelling.dto.request.message.MessageDeleteRequest;
import com.iseeyou.fortunetelling.dto.request.message.MessageRecallRequest;
import com.iseeyou.fortunetelling.dto.response.ChatMessageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MessageService {
    ChatMessageResponse sendMessage(UUID conversationId, ChatMessageRequest request);
    Page<ChatMessageResponse> getMessages(UUID conversationId, Pageable pageable);
    void markMessageAsRead(UUID messageId);
    void deleteMessage(UUID messageId);
    void softDeleteMessages(UUID conversationId, MessageDeleteRequest request);
    void undoDeleteMessages(UUID conversationId);
    void batchSoftDeleteMessages(UUID conversationId, MessageDeleteRequest request);
    Long getRemainingUndoTime(UUID conversationId);
    void recallMessages(UUID conversationId, MessageRecallRequest request);
    boolean canRecallMessage(UUID messageId);
}
