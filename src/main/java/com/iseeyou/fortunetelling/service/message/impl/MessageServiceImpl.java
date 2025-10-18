package com.iseeyou.fortunetelling.service.message.impl;

import com.iseeyou.fortunetelling.dto.request.message.ChatMessageRequest;
import com.iseeyou.fortunetelling.dto.response.ChatMessageResponse;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
        // Validate conversation exists
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found with id: " + conversationId));

        // Validate user is participant (Seer or Customer)
        User currentUser = userService.getUser();
        boolean isParticipant = conversation.getBooking().getCustomer().getId().equals(currentUser.getId()) ||
                conversation.getBooking().getServicePackage().getSeer().getId().equals(currentUser.getId());

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
        Page<Message> messages = messageRepository.findByConversation_IdOrderByCreatedAtDesc(conversationId, pageable);
        return messages.map(msg -> messageMapper.mapTo(msg, ChatMessageResponse.class));
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
}
