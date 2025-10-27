package com.iseeyou.fortunetelling.service.chat.impl;

import com.iseeyou.fortunetelling.dto.request.chat.ChatMessageRequest;
import com.iseeyou.fortunetelling.dto.response.chat.ChatMessageResponse;
import com.iseeyou.fortunetelling.entity.chat.Conversation;
import com.iseeyou.fortunetelling.entity.chat.Message;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.exception.NotFoundException;
import com.iseeyou.fortunetelling.mapper.MessageMapper;
import com.iseeyou.fortunetelling.repository.chat.ConversationRepository;
import com.iseeyou.fortunetelling.repository.chat.MessageRepository;
import com.iseeyou.fortunetelling.service.chat.MessageService;
import com.iseeyou.fortunetelling.service.user.UserService;
import com.iseeyou.fortunetelling.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserService userService;
    private final MessageMapper messageMapper;

    @Override
    @Transactional
    public ChatMessageResponse sendMessage(UUID conversationId, ChatMessageRequest request) {
        return sendMessage(conversationId, request, userService.getUser());
    }

    @Override
    @Transactional
    public ChatMessageResponse sendMessage(UUID conversationId, ChatMessageRequest request, User sender) {
        log.info("Attempting to send message in conversation {} by user {}", conversationId, sender.getId());

        // Validate conversation exists - use JOIN FETCH to avoid lazy loading
        Conversation conversation = conversationRepository.findByIdWithDetails(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found with id: " + conversationId));

        log.debug("Conversation found: {}, Status: {}", conversationId, conversation.getStatus());

        // Validate user is participant (Seer or Customer)
        boolean isCustomer = conversation.getBooking().getCustomer().getId().equals(sender.getId());
        boolean isSeer = conversation.getBooking().getServicePackage().getSeer().getId().equals(sender.getId());
        boolean isParticipant = isCustomer || isSeer;

        if (!isParticipant) {
            log.error("User {} is not a participant in conversation {}", sender.getId(), conversationId);
            throw new IllegalStateException("User is not a participant in this conversation");
        }

        log.debug("User {} is participant (isCustomer: {}, isSeer: {})", sender.getId(), isCustomer, isSeer);

        // Validate conversation is active
        if (!conversation.getStatus().equals(Constants.ConversationStatusEnum.ACTIVE)) {
            log.error("Cannot send message to conversation {} with status {}", conversationId, conversation.getStatus());
            throw new IllegalStateException("Cannot send message to inactive conversation");
        }

        // Create message
        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .textContent(request.getTextContent())
                .imageUrl(request.getImageUrl())
                .videoUrl(request.getVideoUrl())
                .messageType("USER")
                .status(Constants.MessageStatusEnum.UNREAD)
                .deletedBy(null)
                .build();

        Message savedMessage = messageRepository.save(message);
        log.info("Message saved successfully: {} in conversation {} by user {}",
                savedMessage.getId(), conversationId, sender.getId());

        // Map to response - senderId will be set automatically by mapper
        return messageMapper.mapTo(savedMessage, ChatMessageResponse.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> getMessages(UUID conversationId, Pageable pageable) {
        // Get current user
        User currentUser = userService.getUser();

        // Validate conversation exists
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found with id: " + conversationId));

        // Validate user is participant
        boolean isParticipant = conversation.getBooking().getCustomer().getId().equals(currentUser.getId()) ||
                conversation.getBooking().getServicePackage().getSeer().getId().equals(currentUser.getId());

        if (!isParticipant) {
            throw new IllegalStateException("User is not a participant in this conversation");
        }

        // Determine user role to filter deleted messages
        boolean isCustomer = conversation.getBooking().getCustomer().getId().equals(currentUser.getId());
        Constants.RoleEnum userRole = isCustomer ? Constants.RoleEnum.CUSTOMER : Constants.RoleEnum.SEER;

        // Get visible messages (excluding those deleted by current user)
        Page<Message> messages = messageRepository.findVisibleMessages(conversationId, userRole, pageable);

        log.info("Retrieved {} messages for conversation {} for user {}",
                messages.getNumberOfElements(), conversationId, currentUser.getId());

        // Map messages - senderId will be set automatically by mapper
        return messages.map(message -> messageMapper.mapTo(message, ChatMessageResponse.class));
    }


    @Override
    @Transactional
    public void markMessagesAsRead(UUID conversationId) {
        // Get current user
        User currentUser = userService.getUser();

        // Find all unread messages in this conversation that were NOT sent by current user
        List<Message> unreadMessages = messageRepository.findByConversationIdAndStatus(
                conversationId,
                Constants.MessageStatusEnum.UNREAD
        );

        // Filter out messages sent by current user and mark others as read
        List<Message> messagesToMarkAsRead = unreadMessages.stream()
                .filter(message -> !message.getSender().getId().equals(currentUser.getId()))
                .peek(message -> {
                    message.setStatus(Constants.MessageStatusEnum.READ);
                    message.setReadAt(LocalDateTime.now());
                })
                .toList();

        if (!messagesToMarkAsRead.isEmpty()) {
            messageRepository.saveAll(messagesToMarkAsRead);
            log.info("Marked {} messages as read in conversation {} for user {}",
                    messagesToMarkAsRead.size(), conversationId, currentUser.getId());
        }
    }

    @Override
    @Transactional
    public void deleteMessage(UUID messageId) {
        User currentUser = userService.getUser();

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException("Message not found with id: " + messageId));

        // Verify user is in conversation
        Conversation conversation = message.getConversation();
        boolean isParticipant = conversation.getBooking().getCustomer().getId().equals(currentUser.getId()) ||
                conversation.getBooking().getServicePackage().getSeer().getId().equals(currentUser.getId());

        if (!isParticipant) {
            throw new IllegalStateException("User is not a participant in this conversation");
        }

        // Determine role of current user
        boolean isCustomer = conversation.getBooking().getCustomer().getId().equals(currentUser.getId());
        Constants.RoleEnum userRole = isCustomer ? Constants.RoleEnum.CUSTOMER : Constants.RoleEnum.SEER;

        // Check if already deleted by this user
        if (message.getDeletedBy() != null) {
            if (message.getDeletedBy() == userRole) {
                log.warn("Message {} already deleted by {}", messageId, userRole);
                return;
            }
            // If both users delete, mark as DELETED permanently
            message.setStatus(Constants.MessageStatusEnum.DELETED);
            log.info("Message {} deleted by both users, marking as DELETED", messageId);
        } else {
            // First user to delete - mark deletedBy
            message.setDeletedBy(userRole);
            message.setStatus(Constants.MessageStatusEnum.REMOVED);
            log.info("Message {} removed for user {}", messageId, userRole);
        }

        messageRepository.save(message);
    }
}
