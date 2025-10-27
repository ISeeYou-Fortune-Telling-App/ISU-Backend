package com.iseeyou.fortunetelling.service.chat.impl;

import com.iseeyou.fortunetelling.dto.response.chat.ConversationResponse;
import com.iseeyou.fortunetelling.entity.booking.Booking;
import com.iseeyou.fortunetelling.entity.chat.Conversation;
import com.iseeyou.fortunetelling.entity.chat.Message;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.exception.NotFoundException;
import com.iseeyou.fortunetelling.mapper.ConversationMapper;
import com.iseeyou.fortunetelling.repository.booking.BookingRepository;
import com.iseeyou.fortunetelling.repository.chat.ConversationRepository;
import com.iseeyou.fortunetelling.repository.chat.MessageRepository;
import com.iseeyou.fortunetelling.service.MessageSourceService;
import com.iseeyou.fortunetelling.service.chat.ConversationService;
import com.iseeyou.fortunetelling.service.user.UserService;
import com.iseeyou.fortunetelling.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationServiceImpl implements ConversationService {

    private final MessageSourceService messageSourceService;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ConversationMapper conversationMapper;
    private final BookingRepository bookingRepository;
    private final UserService userService;

    @Override
    @Transactional
    public ConversationResponse createChatSession(UUID bookingId) {
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
            return conversationMapper.mapTo(existingConversation.get(), ConversationResponse.class);
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

        return conversationMapper.mapTo(savedConversation, ConversationResponse.class);
    }

    @Override
    @Transactional
    public ConversationResponse getConversation(UUID conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found with id: " + conversationId));
        return conversationMapper.mapTo(conversation, ConversationResponse.class);
    }

    @Override
    @Transactional
    public ConversationResponse getChatSessionByBookingId(UUID bookingId) {
        Conversation conversation = conversationRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new NotFoundException("Chat session not found for booking: " + bookingId));
        return conversationMapper.mapTo(conversation, ConversationResponse.class);
    }

    @Override
    @Transactional
    public Page<ConversationResponse> getMyChatSessions(Pageable pageable) {
        User currentUser = userService.getUser();
        Page<Conversation> conversations;

        if (currentUser.getRole().equals(Constants.RoleEnum.SEER)) {
            conversations = conversationRepository.findByBooking_ServicePackage_Seer_Id(currentUser.getId(), pageable);
        } else {
            conversations = conversationRepository.findByBooking_Customer_Id(currentUser.getId(), pageable);
        }

        return conversations.map(conv -> conversationMapper.mapTo(conv, ConversationResponse.class));
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

        // Determine who is late
        boolean customerLate = conversation.getCustomerJoinedAt() == null;
        boolean seerLate = conversation.getSeerJoinedAt() == null;

        String canceledBy;
        if (customerLate && seerLate) {
            canceledBy = "BOTH";
        } else if (customerLate) {
            canceledBy = "CUSTOMER";
        } else {
            canceledBy = "SEER";
        }

        // Cancel conversation
        conversation.setStatus(Constants.ConversationStatusEnum.CANCELLED);
        conversation.setCanceledBy(canceledBy);
        conversationRepository.save(conversation);

        // Cancel booking
        Booking booking = conversation.getBooking();
        booking.setStatus(Constants.BookingStatusEnum.CANCELED);
        bookingRepository.save(booking);

        log.info("Session canceled due to late join: conversation={}, booking={}, canceledBy={}",
                conversationId, booking.getId(), canceledBy);
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
    @Transactional
    public void extendSession(UUID conversationId, Integer additionalMinutes) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));

        // Extend session end time
        LocalDateTime newEndTime = conversation.getSessionEndTime().plusMinutes(additionalMinutes);
        conversation.setSessionEndTime(newEndTime);
        conversation.setExtendedMinutes(conversation.getExtendedMinutes() + additionalMinutes);
        conversationRepository.save(conversation);

        log.info("Session extended: conversation={}, additionalMinutes={}, newEndTime={}",
                conversationId, additionalMinutes, newEndTime);
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
                .sender(booking.getServicePackage().getSeer())
                .textContent(messageContent)
                .messageType("SYSTEM")
                .status(Constants.MessageStatusEnum.UNREAD)
                .deletedBy(null)
                .build();
    }
}
