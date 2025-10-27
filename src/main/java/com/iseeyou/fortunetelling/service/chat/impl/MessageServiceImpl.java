package com.iseeyou.fortunetelling.service.chat.impl;

import com.iseeyou.fortunetelling.dto.request.message.ChatMessageRequest;
import com.iseeyou.fortunetelling.dto.response.ChatMessageResponse;
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
    public ChatMessageResponse sendMessage(UUID conversationId, ChatMessageRequest request, User sender) {
        // Validate conversation exists
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found with id: " + conversationId));

        // Validate user is participant (Seer or Customer)
        boolean isParticipant = conversation.getBooking().getCustomer().getId().equals(sender.getId()) ||
                conversation.getBooking().getServicePackage().getSeer().getId().equals(sender.getId());

        if (!isParticipant) {
            throw new IllegalStateException("User is not a participant in this conversation");
        }

        // Validate conversation is active
        if (!conversation.getStatus().equals(Constants.ConversationStatusEnum.ACTIVE)) {
            throw new IllegalStateException("Cannot send message to inactive conversation");
        }

        // Create message
        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .textContent(request.getTextContent())
                .imageUrl(request.getImageUrl())
                .videoUrl(request.getVideoUrl())
                .messageType(Constants.MessageTypeEnum.USER.getValue())
                .isRead(false)
                .isDeleted(false)
                .isRemoved(false)
                .build();

        Message savedMessage = messageRepository.save(message);
        log.info("Message sent in conversation {} by user {}", conversationId, sender.getId());

        return messageMapper.mapTo(savedMessage, ChatMessageResponse.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> getMessages(UUID conversationId, Pageable pageable) {
        Page<Message> messages = messageRepository.findByConversation_IdOrderByCreatedAtDesc(conversationId, pageable);
        return messages.map(msg -> messageMapper.mapTo(msg, ChatMessageResponse.class));
    }

    @Override
    @Transactional
    public void markMessagesAsRead(UUID conversationId) {
        // Get current user
        User currentUser = userService.getUser();

        // Find all unread messages in this conversation that were NOT sent by current user
        List<Message> unreadMessages = messageRepository.findByConversationIdAndIsReadFalse(conversationId);

        // Filter out messages sent by current user and mark others as read
        List<Message> messagesToMarkAsRead = unreadMessages.stream()
                .filter(message -> !message.getSender().getId().equals(currentUser.getId()))
                .peek(message -> {
                    message.setIsRead(true);
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
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException("Message not found with id: " + messageId));
        message.setIsDeleted(true);
        messageRepository.save(message);
    }
}
