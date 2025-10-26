package com.iseeyou.fortunetelling.service.message.impl;

import com.iseeyou.fortunetelling.dto.request.message.ChatMessageRequest;
import com.iseeyou.fortunetelling.dto.request.message.MessageDeleteRequest;
import com.iseeyou.fortunetelling.dto.request.message.MessageRecallRequest;
import com.iseeyou.fortunetelling.dto.response.ChatMessageResponse;
import com.iseeyou.fortunetelling.dto.Internal.DeletedMessageInfo;
import com.iseeyou.fortunetelling.entity.Conversation;
import com.iseeyou.fortunetelling.entity.Message;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.exception.NotFoundException;
import com.iseeyou.fortunetelling.mapper.MessageMapper;
import com.iseeyou.fortunetelling.repository.converstation.ConversationRepository;
import com.iseeyou.fortunetelling.repository.message.MessageRepository;
import com.iseeyou.fortunetelling.service.message.MessageService;
import com.iseeyou.fortunetelling.service.user.UserService;
import com.iseeyou.fortunetelling.util.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserService userService;
    private final MessageMapper messageMapper;
    private final MessageCacheService messageCacheService;

    @Value("${app.message.recall-time-limit-minutes:15}")
    private int recallTimeLimitMinutes;

    @Override
    @Transactional
    public ChatMessageResponse sendMessage(UUID conversationId, ChatMessageRequest request) {

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found with id: " + conversationId));

        User currentUser = userService.getUser();

        // Validate conversation exists
        validateConversationAccess(conversationId, currentUser.getId());

        // Validate conversation is active
        if (!conversation.getStatus().equals(Constants.ConversationStatusEnum.ACTIVE)) {
            throw new IllegalStateException("Cannot send message to inactive conversation");
        }

        // Create message
        Message message = Message.builder()
                .conversation(conversation)
                .sender(currentUser)
                .textContent(request.getTextContent())
                .imageUrl(request.getImageUrl())
                .videoUrl(request.getVideoUrl())
                .messageType(Constants.MessageTypeEnum.USER.getValue())
                .isRead(false)
                .isDeleted(false)
                .isRemoved(false)
                .build();

        Message savedMessage = messageRepository.save(message);
        log.info("Message sent in conversation {} by user {}", conversationId, currentUser.getId());

        return messageMapper.mapTo(savedMessage, ChatMessageResponse.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> getMessages(UUID conversationId, Pageable pageable) {
        User currentUser = userService.getUser();

        Page<Message> messages = messageRepository.findVisibleMessagesByConversationAndUser(
                conversationId,
                currentUser.getId(),
                pageable
        );

        LocalDateTime recallTimeLimit = LocalDateTime.now().minusMinutes(recallTimeLimitMinutes);

        return messages.map(msg -> {
            ChatMessageResponse response = messageMapper.mapTo(msg, ChatMessageResponse.class);

            // Calculate canRecall server-side
            boolean canRecall = msg.getSender().getId().equals(currentUser.getId())
                    && !msg.getIsRecalled()
                    && msg.getCreatedAt().isAfter(recallTimeLimit);

            response.setCanRecall(canRecall);
            return response;
        });
    }

    @Override
    @Transactional
    public void markMessageAsRead(UUID messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException("Message not found with id: " + messageId));
        message.setIsRead(true);
        message.setReadAt(LocalDateTime.now());
        messageRepository.save(message);
    }

    @Override
    @Transactional
    public void deleteMessage(UUID messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException("Message not found with id: " + messageId));
        message.setIsDeleted(true);
        messageRepository.save(message);
    }

    @Override
    public void softDeleteMessages(UUID conversationId, MessageDeleteRequest request) {
        User currentUser = userService.getUser();

        // Validate conversation access
        validateConversationAccess(conversationId, currentUser.getId());

        // Get messages and validate they belong to the conversation
        List<Message> messages = messageRepository.findByIdsAndConversation(
                request.getMessageIds(),
                conversationId
        );

        if (messages.isEmpty()) {
            throw new NotFoundException("No messages found to delete");
        }

        // Soft delete for current user only
        for (Message message : messages) {
            if (!message.getDeletedByUserIds().contains(currentUser.getId())) {
                message.getDeletedByUserIds().add(currentUser.getId());
            }
        }

        messageRepository.saveAll(messages);

        // Cache to Redis for undo feature (auto-expire after 30s)
        messageCacheService.cacheDeletedMessages(
                currentUser.getId(),
                conversationId,
                request.getMessageIds()
        );

        log.info("User {} soft deleted {} messages in conversation {} (cached for undo)",
                currentUser.getId(), messages.size(), conversationId);
    }

    @Override
    public void undoDeleteMessages(UUID conversationId) {
        User currentUser = userService.getUser();

        // Validate conversation access
        validateConversationAccess(conversationId, currentUser.getId());

        // Get deleted message info from Redis
        Optional<DeletedMessageInfo> cachedInfo = messageCacheService.getDeletedMessages(
                currentUser.getId(),
                conversationId
        );

        if (cachedInfo.isEmpty()) {
            throw new IllegalStateException(
                    "No recently deleted messages found or undo time limit exceeded (30 seconds)"
            );
        }

        DeletedMessageInfo info = cachedInfo.get();

        // Find messages that were deleted by this user
        List<Message> messages = messageRepository.findDeletedMessagesByUser(
                info.getMessageIds(),
                currentUser.getId(),
                conversationId
        );

        if (messages.isEmpty()) {
            throw new NotFoundException("Messages not found or already restored");
        }

        // Restore messages for current user
        for (Message message : messages) {
            message.getDeletedByUserIds().remove(currentUser.getId());
        }

        messageRepository.saveAll(messages);

        // Remove from Redis cache
        messageCacheService.removeCachedMessages(currentUser.getId(), conversationId);

        log.info("User {} restored {} messages in conversation {}",
                currentUser.getId(), messages.size(), conversationId);
    }

    @Override
    public void batchSoftDeleteMessages(UUID conversationId, MessageDeleteRequest request) {
        // Validate batch size
        if (request.getMessageIds().size() > 50) {
            throw new IllegalArgumentException("Cannot delete more than 50 messages at once");
        }

        User currentUser = userService.getUser();

        // Check if there's existing cached deletion
        Optional<DeletedMessageInfo> existingCache = messageCacheService.getDeletedMessages(
                currentUser.getId(),
                conversationId
        );

        if (existingCache.isPresent()) {
            // Append to existing cache and refresh TTL
            messageCacheService.appendDeletedMessages(
                    currentUser.getId(),
                    conversationId,
                    request.getMessageIds()
            );
        }

        // Use the same soft delete logic
        softDeleteMessages(conversationId, request);
    }

    @Override
    public Long getRemainingUndoTime(UUID conversationId) {
        User currentUser = userService.getUser();
        return messageCacheService.getRemainingUndoTime(currentUser.getId(), conversationId);
    }

    @Override
    public void recallMessages(UUID conversationId, MessageRecallRequest request) {
        User currentUser = userService.getUser();
        validateConversationAccess(conversationId, currentUser.getId());

        // Validate: Only sender can recall their own messages
        List<Message> messages = messageRepository.findRecallableMessages(
                request.getMessageIds(),
                conversationId,
                currentUser.getId()
        );

        if (messages.isEmpty()) {
            throw new IllegalStateException(
                    "No messages found to recall. You can only recall your own messages."
            );
        }

        if (messages.size() != request.getMessageIds().size()) {
            throw new IllegalStateException(
                    "Some messages cannot be recalled (not yours or already recalled)"
            );
        }

        // Validate time limit
        LocalDateTime recallTimeLimit = LocalDateTime.now().minusMinutes(recallTimeLimitMinutes);
        List<Message> validMessages = messageRepository.findMessagesWithinRecallLimit(
                request.getMessageIds(),
                currentUser.getId(),
                recallTimeLimit
        );

        if (validMessages.size() != messages.size()) {
            throw new IllegalStateException(
                    String.format("Some messages are too old to recall (limit: %d minutes)",
                            recallTimeLimitMinutes)
            );
        }

        // Recall messages (delete for everyone)
        LocalDateTime now = LocalDateTime.now();
        for (Message message : messages) {
            message.setIsRecalled(true);
            message.setRecalledAt(now);
            message.setRecalledBy(currentUser);
        }

        messageRepository.saveAll(messages);

        log.info("User {} recalled {} messages in conversation {}",
                currentUser.getId(), messages.size(), conversationId);
    }

    @Override
    public boolean canRecallMessage(UUID messageId) {
        User currentUser = userService.getUser();

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException("Message not found"));

        // Check: is sender
        if (!message.getSender().getId().equals(currentUser.getId())) {
            return false;
        }

        // Check: not already recalled
        if (message.getIsRecalled()) {
            return false;
        }

        // Check: within time limit
        LocalDateTime recallTimeLimit = LocalDateTime.now().minusMinutes(recallTimeLimitMinutes);
        return message.getCreatedAt().isAfter(recallTimeLimit);
    }

    private void validateConversationAccess(UUID conversationId, UUID currentUserId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found with id: " + conversationId));


        boolean isParticipant = conversation.getBooking().getCustomer().getId().equals(currentUserId) ||
                conversation.getBooking().getServicePackage().getSeer().getId().equals(currentUserId);

        if (!isParticipant) {
            throw new IllegalStateException("User is not a participant in this conversation");
        }
    }
}
