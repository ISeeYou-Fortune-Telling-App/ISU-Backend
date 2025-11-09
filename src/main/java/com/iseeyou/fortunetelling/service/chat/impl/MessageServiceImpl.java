package com.iseeyou.fortunetelling.service.chat.impl;

import com.iseeyou.fortunetelling.dto.request.chat.session.ChatMessageRequest;
import com.iseeyou.fortunetelling.dto.response.chat.session.AdminMessageStatisticResponse;
import com.iseeyou.fortunetelling.dto.response.chat.session.ChatMessageResponse;
import com.iseeyou.fortunetelling.entity.chat.Conversation;
import com.iseeyou.fortunetelling.entity.chat.Message;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.exception.NotFoundException;
import com.iseeyou.fortunetelling.mapper.MessageMapper;
import com.iseeyou.fortunetelling.repository.chat.ConversationRepository;
import com.iseeyou.fortunetelling.repository.chat.MessageRepository;
import com.iseeyou.fortunetelling.repository.user.UserRepository;
import com.iseeyou.fortunetelling.service.chat.MessageService;
import com.iseeyou.fortunetelling.service.fileupload.CloudinaryService;
import com.iseeyou.fortunetelling.service.user.UserService;
import com.iseeyou.fortunetelling.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
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
    private final CloudinaryService cloudinaryService;
    private final UserRepository userRepository;

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

        log.debug("Conversation found: {}, Type: {}, Status: {}",
                conversationId, conversation.getType(), conversation.getStatus());

        // Validate user is participant
        boolean isParticipant = false;

        if (conversation.getType() == Constants.ConversationTypeEnum.ADMIN_CHAT) {
            // Admin chat: check if user is admin or target user
            isParticipant = (conversation.getAdmin() != null && conversation.getAdmin().getId().equals(sender.getId())) ||
                          (conversation.getTargetUser() != null && conversation.getTargetUser().getId().equals(sender.getId()));
        } else if (conversation.getBooking() != null) {
            // Booking session: check if user is customer or seer
            boolean isCustomer = conversation.getBooking().getCustomer().getId().equals(sender.getId());
            boolean isSeer = conversation.getBooking().getServicePackage().getSeer().getId().equals(sender.getId());
            isParticipant = isCustomer || isSeer;
        }

        if (!isParticipant) {
            log.error("User {} is not a participant in conversation {}", sender.getId(), conversationId);
            throw new IllegalStateException("User is not a participant in this conversation");
        }

        log.debug("User {} is participant in conversation {}", sender.getId(), conversationId);

        // Validate conversation is active
        if (!conversation.getStatus().equals(Constants.ConversationStatusEnum.ACTIVE)) {
            log.error("Cannot send message to conversation {} with status {}", conversationId, conversation.getStatus());
            throw new IllegalStateException("Cannot send message to inactive conversation");
        }

        // Upload files if present
        String imageUrl = null;
        String videoUrl = null;

        try {
            if (request.getImage() != null && !request.getImage().isEmpty()) {
                log.info("Uploading image for message in conversation {}", conversationId);
                imageUrl = cloudinaryService.uploadFile(request.getImage(), "chat/images");
                log.info("Image uploaded successfully: {}", imageUrl);
            }

            if (request.getVideo() != null && !request.getVideo().isEmpty()) {
                log.info("Uploading video for message in conversation {}", conversationId);
                videoUrl = cloudinaryService.uploadFile(request.getVideo(), "chat/videos");
                log.info("Video uploaded successfully: {}", videoUrl);
            }
        } catch (IOException e) {
            log.error("Failed to upload files for message in conversation {}", conversationId, e);
            throw new RuntimeException("Failed to upload files: " + e.getMessage(), e);
        }

        // Create message
        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .textContent(request.getTextContent())
                .imageUrl(imageUrl)
                .videoUrl(videoUrl)
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
    @Transactional
    public boolean sendMessages(List<UUID> conversationIds, ChatMessageRequest request, User sender) {
        try {
            for  (UUID conversationId : conversationIds) {
                sendMessage(conversationId, request);
            }
        } catch (RuntimeException e) {
            log.error("Failed to send messages in conversation {}", conversationIds, e);
            return false;
        }
        return true;
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
        boolean isParticipant = false;
        Constants.RoleEnum userRole = null;

        Constants.RoleEnum currentRole = currentUser.getRole();

        if (conversation.getType() == Constants.ConversationTypeEnum.ADMIN_CHAT) {
            // Admin chat: check if user is admin or target user
            boolean isAdmin = conversation.getAdmin() != null && conversation.getAdmin().getId().equals(currentUser.getId());
            boolean isTarget = conversation.getTargetUser() != null && conversation.getTargetUser().getId().equals(currentUser.getId());

            isParticipant = isAdmin || isTarget;

            // For admin chat, use actual user role for filtering deleted messages
            userRole = currentRole;
        } else if (conversation.getBooking() != null) {
            // Booking session: check if user is customer or seer
            boolean isCustomer = conversation.getBooking().getCustomer().getId().equals(currentUser.getId());
            boolean isSeer = conversation.getBooking().getServicePackage().getSeer().getId().equals(currentUser.getId());

            isParticipant = isCustomer || isSeer;
            userRole = isCustomer ? Constants.RoleEnum.CUSTOMER : Constants.RoleEnum.SEER;
        }

        // Allow platform admins to access any conversation even if not explicit participants
        if (currentRole == Constants.RoleEnum.ADMIN) {
            isParticipant = true;
            // For admin, do not exclude messages deleted by any role so admin can see all messages
            userRole = null;
        }

        if (!isParticipant) {
            throw new IllegalStateException("User is not a participant in this conversation");
        }


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
        boolean isParticipant = false;
        Constants.RoleEnum userRole = null;

        if (conversation.getType() == Constants.ConversationTypeEnum.ADMIN_CHAT) {
            // Admin chat: check if user is admin or target user
            boolean isAdmin = conversation.getAdmin() != null &&
                            conversation.getAdmin().getId().equals(currentUser.getId());
            boolean isTarget = conversation.getTargetUser() != null &&
                             conversation.getTargetUser().getId().equals(currentUser.getId());

            isParticipant = isAdmin || isTarget;
            userRole = currentUser.getRole(); // Use actual user role
        } else if (conversation.getBooking() != null) {
            // Booking session: check if user is customer or seer
            boolean isCustomer = conversation.getBooking().getCustomer().getId().equals(currentUser.getId());
            boolean isSeer = conversation.getBooking().getServicePackage().getSeer().getId().equals(currentUser.getId());

            isParticipant = isCustomer || isSeer;
            userRole = isCustomer ? Constants.RoleEnum.CUSTOMER : Constants.RoleEnum.SEER;
        }

        if (!isParticipant) {
            throw new IllegalStateException("User is not a participant in this conversation");
        }


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

    @Override
    @Transactional(readOnly = true)
    public AdminMessageStatisticResponse getMessageStatistics() {
        AdminMessageStatisticResponse response = new AdminMessageStatisticResponse();

        long totalUsers = userRepository.count() - 1;
        long totalActives = userRepository.countAllByStatus(Constants.StatusProfileEnum.ACTIVE) - 1;
        User admin = userService.getUser();
        long totalSent = messageRepository.countAllBySender(admin);
        long totalSentAndRead = messageRepository.countAllBySenderAndStatus(admin, Constants.MessageStatusEnum.READ);
        Double readPercent = (double) (totalSentAndRead * 100 / totalSent);

        response.setTotalSentMessages(totalSent);
        response.setReadPercent(readPercent);
        response.setTotalUsers(totalUsers);
        response.setTotalActives(totalActives);

        return response;
    }
}
