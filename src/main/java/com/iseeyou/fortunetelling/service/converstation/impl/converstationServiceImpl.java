package com.iseeyou.fortunetelling.service.converstation.impl;

import com.iseeyou.fortunetelling.dto.request.converstation.ChatHistoryFilterRequest;
import com.iseeyou.fortunetelling.dto.response.converstation.ChatSessionResponse;
import com.iseeyou.fortunetelling.entity.Conversation;
import com.iseeyou.fortunetelling.entity.Message;
import com.iseeyou.fortunetelling.entity.booking.Booking;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.exception.NotFoundException;
import com.iseeyou.fortunetelling.mapper.ConversationMapper;
import com.iseeyou.fortunetelling.repository.booking.BookingRepository;
import com.iseeyou.fortunetelling.repository.converstation.ConversationRepository;
import com.iseeyou.fortunetelling.service.MessageSourceService;
import com.iseeyou.fortunetelling.service.converstation.ConversationService;
import com.iseeyou.fortunetelling.service.user.UserService;
import com.iseeyou.fortunetelling.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class converstationServiceImpl implements ConversationService {

    private final MessageSourceService messageSourceService;
    private final ConversationRepository conversationRepository;
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ConversationMapper conversationMapper;

    @Override
    @Transactional
    public ChatSessionResponse createChatSession(UUID bookingId) {
        // 1. Kiểm tra booking tồn tại và trạng thái của nó
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found with id: " + bookingId));

        if (!booking.getStatus().equals(Constants.BookingStatusEnum.CONFIRMED)) {
            throw new IllegalStateException(
                    "Cannot create chat session for booking with status: " + booking.getStatus()
            );
        }

        // 2. Kiểm tra phiên chat đã tồn tại
        Optional<Conversation> existingConversation = conversationRepository.findByBookingId(bookingId);
        if (existingConversation.isPresent()) {
            log.warn("Chat session already exists for booking: {}", bookingId);
            return conversationMapper.mapTo(existingConversation.get(), ChatSessionResponse.class);
        }

        // 3. Tính toán thời gian phiên
        LocalDateTime sessionStartTime = LocalDateTime.now();
        Integer sessionDurationMinutes = booking.getServicePackage().getDurationMinutes();
        LocalDateTime sessionEndTime = sessionStartTime.plusMinutes(sessionDurationMinutes);

        // 4. Tạo phiên chat mới
        Conversation conversation = Conversation.builder()
                .booking(booking)
                .type(Constants.ConversationTypeEnum.BOOKING_SESSION)
                .sessionStartTime(sessionStartTime)
                .sessionEndTime(sessionEndTime)
                .sessionDurationMinutes(sessionDurationMinutes)
                .status(Constants.ConversationStatusEnum.ACTIVE)
                .messages(new HashSet<>())
                .build();

        Conversation savedConversation = conversationRepository.save(conversation);
        log.info("Chat session created successfully for booking: {} between seer: {} and customer: {}",
                bookingId, booking.getServicePackage().getSeer().getId(), booking.getCustomer().getId());

        // 5. Tạo message khởi tạo
        Message initiationMessage = createInitiationMessage(savedConversation, booking);
        savedConversation.getMessages().add(initiationMessage);
        conversationRepository.save(savedConversation);

        return conversationMapper.mapTo(savedConversation, ChatSessionResponse.class);
    }

    @Override
    @Transactional
    public ChatSessionResponse getChatSessionByBookingId(UUID bookingId) {
        Conversation conversation = conversationRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new NotFoundException("Chat session not found for booking: " + bookingId));
        return conversationMapper.mapTo(conversation, ChatSessionResponse.class);
    }

    @Override
    @Transactional
    public Page<ChatSessionResponse> getMyChatSessions(Pageable pageable) {
        User currentUser = userService.getUser();
        Page<Conversation> conversations;

        if (currentUser.getRole().equals(Constants.RoleEnum.SEER)) {
            conversations = conversationRepository.findByBooking_ServicePackage_Seer(currentUser.getId(), pageable);
        } else {
            conversations = conversationRepository.findByBooking_Customer(currentUser.getId(), pageable);
        }

        return conversations.map(conv -> conversationMapper.mapTo(conv, ChatSessionResponse.class));
    }

    @Override
    @Transactional
    public void endChatSession(UUID conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found with id: " + conversationId));

        conversation.setStatus(Constants.ConversationStatusEnum.ENDED);
        conversation.setSessionEndTime(LocalDateTime.now());
        conversationRepository.save(conversation);
        log.info("Chat session ended for conversation: {}", conversationId);
    }

    @Override
    @Transactional
    public void cancelLateSession(UUID conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));

        // Cancel conversation
        conversation.setStatus(Constants.ConversationStatusEnum.CANCELLED);
        conversation.setCancelReason("Customer late >10 minutes");
        conversationRepository.save(conversation);

        // Cancel booking
        Booking booking = conversation.getBooking();
        booking.setStatus(Constants.BookingStatusEnum.CANCELED);
        bookingRepository.save(booking);

        log.info("Session canceled due to customer late: conversation={}, booking={}",
                conversationId, booking.getId());
    }

    @Override
    @Transactional
    public void sendWarningNotification(UUID conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));

        // Mark warning sent
        conversation.setWarningNotificationSent(true);
        conversationRepository.save(conversation);

        log.info("Warning notification sent for conversation: {}", conversationId);
    }

    @Override
    @Transactional
    public void autoEndSession(UUID conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));

        // End conversation
        conversation.setStatus(Constants.ConversationStatusEnum.ENDED);
        conversation.setSessionEndTime(LocalDateTime.now());
        conversationRepository.save(conversation);

        // Complete booking
        Booking booking = conversation.getBooking();
        booking.setStatus(Constants.BookingStatusEnum.COMPLETED);
        bookingRepository.save(booking);

        log.info("Session auto-ended: conversation={}, booking={}",
                conversationId, booking.getId());
    }

    @Override
    public void extendSession(UUID conversationId, Integer additionalMinutes) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));

        // Extend session
        LocalDateTime newEndTime = conversation.getSessionEndTime().plusMinutes(additionalMinutes);
        conversation.setSessionEndTime(newEndTime);
        conversation.setExtendedMinutes(conversation.getExtendedMinutes() + additionalMinutes);
        conversation.setWarningNotificationSent(false); // Reset warning flag
        conversationRepository.save(conversation);

        log.info("Session extended by {} minutes: conversation={}", additionalMinutes, conversationId);
    }

    @Override
    public Page<ChatSessionResponse> getChatHistory(ChatHistoryFilterRequest filter, Pageable pageable) {
        User currentUser = userService.getUser();
        Page<Conversation> conversations;

        // ADMIN: Full filters (participant name, type, statuses)
        if (currentUser.getRole().equals(Constants.RoleEnum.ADMIN)) {
            String participantName = filter.getParticipantName();
            Constants.ConversationTypeEnum conversationType = filter.getConversationType();
            List<Constants.ConversationStatusEnum> statuses = filter.getStatuses();

            // Default to all statuses if not provided
            if (statuses == null || statuses.isEmpty()) {
                statuses = Arrays.asList(
                        Constants.ConversationStatusEnum.ACTIVE,
                        Constants.ConversationStatusEnum.ENDED,
                        Constants.ConversationStatusEnum.CANCELLED
                );
            }

            conversations = conversationRepository.findChatHistoryWithFilters(
                    participantName,
                    conversationType,
                    statuses,
                    pageable
            );
        }
        // NON-ADMIN: Search by MESSAGE CONTENT only
        else {
            String messageContent = filter.getMessageContent();

            conversations = conversationRepository.searchUserConversationsByMessageContent(
                    currentUser.getId(),
                    messageContent,
                    pageable
            );
        }

        return conversations.map(conv -> conversationMapper.mapTo(conv, ChatSessionResponse.class));
    }

    private Message createInitiationMessage(Conversation conversation, Booking booking) {
        String messageContent = String.format(
                messageSourceService.get("chat.session.started"),
                booking.getServicePackage().getSeer().getFullName(),
                booking.getServicePackage().getPackageTitle(),
                booking.getServicePackage().getDurationMinutes()
        );

        return Message.builder()
                .conversation(conversation)
                .sender(booking.getServicePackage().getSeer()) // Hệ thống gửi từ Seer
                .textContent(messageContent)
                .messageType("SYSTEM")
                .isRead(false)
                .isDeleted(false)
                .isRemoved(false)
                .build();
    }
}
