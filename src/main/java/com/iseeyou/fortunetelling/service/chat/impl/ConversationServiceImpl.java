package com.iseeyou.fortunetelling.service.chat.impl;

import com.iseeyou.fortunetelling.dto.response.chat.session.ConversationResponse;
import com.iseeyou.fortunetelling.dto.response.chat.session.ConversationStatisticResponse;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

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
        LocalDateTime scheduledTime = booking.getScheduledTime();
        LocalDateTime now = LocalDateTime.now();
        Integer sessionDurationMinutes = booking.getServicePackage().getDurationMinutes();

        // Xác định sessionStartTime và status
        LocalDateTime sessionStartTime;
        Constants.ConversationStatusEnum initialStatus;

        if (scheduledTime != null && scheduledTime.isAfter(now)) {
            // Booking có scheduled time trong tương lai -> tạo conversation WAITING
            sessionStartTime = scheduledTime;
            initialStatus = Constants.ConversationStatusEnum.WAITING;
            log.info("Creating WAITING conversation for future booking at: {}", scheduledTime);
        } else {
            // Booking không có scheduled time hoặc đã đến giờ -> tạo conversation ACTIVE ngay
            sessionStartTime = now;
            initialStatus = Constants.ConversationStatusEnum.ACTIVE;
            log.info("Creating ACTIVE conversation for immediate booking");
        }

        LocalDateTime sessionEndTime = sessionStartTime.plusMinutes(sessionDurationMinutes);

        // 4. Tạo phiên chat mới
        Conversation conversation = Conversation.builder()
                .booking(booking)
                .type(Constants.ConversationTypeEnum.BOOKING_SESSION)
                .sessionStartTime(sessionStartTime)
                .sessionEndTime(sessionEndTime)
                .sessionDurationMinutes(sessionDurationMinutes)
                .status(initialStatus)
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
    public ConversationResponse createAdminConversation(UUID targetUserId, String initialMessage) {
        // 1. Get current admin user
        User admin = userService.getUser();

        // Verify user is admin
        if (!admin.getRole().equals(Constants.RoleEnum.ADMIN)) {
            throw new IllegalStateException("Only admin can create admin conversations");
        }

        // 2. Get target user (customer or seer)
        User targetUser = userService.findById(targetUserId);
        if (targetUser == null) {
            throw new NotFoundException("Target user not found with id: " + targetUserId);
        }

        // 3. Check if admin conversation already exists with this user
        Optional<Conversation> existingConversation = conversationRepository
                .findAdminConversationByAdminAndTarget(admin.getId(), targetUserId);

        if (existingConversation.isPresent()) {
            log.info("Admin conversation already exists between admin {} and user {}",
                    admin.getId(), targetUserId);
            return conversationMapper.mapTo(existingConversation.get(), ConversationResponse.class);
        }

        // 4. Create admin conversation
        Conversation conversation = Conversation.builder()
                .booking(null)  // No booking for admin chat
                .type(Constants.ConversationTypeEnum.ADMIN_CHAT)
                .admin(admin)
                .targetUser(targetUser)
                .sessionStartTime(LocalDateTime.now())
                .sessionEndTime(null)  // No end time for admin chat
                .sessionDurationMinutes(null)  // No duration limit
                .status(Constants.ConversationStatusEnum.ACTIVE)
                .messages(new HashSet<>())
                .build();

        Conversation savedConversation = conversationRepository.save(conversation);
        log.info("Admin conversation created successfully between admin: {} and user: {} ({})",
                admin.getId(), targetUser.getId(), targetUser.getRole());

        // 5. Create initial message if provided
        if (initialMessage != null && !initialMessage.trim().isEmpty()) {
            Message message = Message.builder()
                    .conversation(savedConversation)
                    .sender(admin)
                    .textContent(initialMessage)
                    .messageType("SYSTEM")
                    .status(Constants.MessageStatusEnum.UNREAD)
                    .build();

            savedConversation.getMessages().add(message);
            conversationRepository.save(savedConversation);
        }

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
    @Transactional(readOnly = true)
    public Page<ConversationResponse> getMyChatSessions(Pageable pageable) {
        User currentUser = userService.getUser();
        Page<Conversation> conversations;

        if (currentUser.getRole().equals(Constants.RoleEnum.ADMIN)) {
            // Admin: get all admin conversations where admin is the creator
            conversations = conversationRepository.findAdminConversationsByAdmin(pageable);
            log.info("Admin {} retrieved {} admin conversations", currentUser.getId(), conversations.getTotalElements());
        } else if (currentUser.getRole().equals(Constants.RoleEnum.SEER)) {
            // Seer: get ALL conversations (booking conversations + admin conversations as target)
            conversations = conversationRepository.findAllConversationsBySeer(currentUser.getId(), pageable);
            log.info("Seer {} retrieved {} total conversations", currentUser.getId(), conversations.getTotalElements());
        } else {
            // Customer: get ALL conversations (booking conversations + admin conversations as target)
            // We ignore the sort parameters from controller and sort by latest message time instead

            List<Conversation> conversationList = conversationRepository.findAllConversationsByCustomer(currentUser.getId());
            // Batch fetch latest message times to avoid N+1 queries
            List<UUID> convIds = conversationList.stream().map(Conversation::getId).toList();
            Map<UUID, LocalDateTime> conversationAndItsLatestTimeSent = new HashMap<>();
            if (!convIds.isEmpty()) {
                List<Object[]> pairs = messageRepository.findLatestMessageCreatedAtByConversationIds(convIds);
                for (Object[] pair : pairs) {
                    UUID id = (UUID) pair[0];
                    LocalDateTime ts = (LocalDateTime) pair[1];
                    conversationAndItsLatestTimeSent.put(id, ts);
                }
            }

            // Sort conversations by latest message time (descending). Conversations with no messages go last.
            conversationList.sort((c1, c2) -> {
                LocalDateTime t1 = conversationAndItsLatestTimeSent.get(c1.getId());
                LocalDateTime t2 = conversationAndItsLatestTimeSent.get(c2.getId());

                if (t1 == null && t2 == null) {
                    // Fallback to updatedAt descending
                    if (c1.getUpdatedAt() == null && c2.getUpdatedAt() == null) {
                        return c2.getId().compareTo(c1.getId());
                    }
                    if (c1.getUpdatedAt() == null) return 1;
                    if (c2.getUpdatedAt() == null) return -1;
                    return c2.getUpdatedAt().compareTo(c1.getUpdatedAt());
                }
                if (t1 == null) return 1; // c1 after c2
                if (t2 == null) return -1; // c1 before c2

                // descending order by timeSent
                int cmp = t2.compareTo(t1);
                if (cmp != 0) return cmp;

                // tie-breaker: updatedAt descending
                if (c1.getUpdatedAt() == null && c2.getUpdatedAt() == null) {
                    return c2.getId().compareTo(c1.getId());
                }
                if (c1.getUpdatedAt() == null) return 1;
                if (c2.getUpdatedAt() == null) return -1;
                return c2.getUpdatedAt().compareTo(c1.getUpdatedAt());
            });

            // Calculate start and end indexes for pagination (ignore sort from pageable)
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), conversationList.size());

            List<Conversation> pagedList = conversationList.subList(start, end);

            // Create unsorted pageable to avoid confusion
            Pageable unsortedPageable = org.springframework.data.domain.PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize()
            );
            conversations = new PageImpl<>(pagedList, unsortedPageable, conversationList.size());

            log.info("Customer {} retrieved {} total conversations", currentUser.getId(), conversations.getTotalElements());
        }

        return conversations.map(conv -> conversationMapper.mapTo(conv, ConversationResponse.class));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConversationResponse> getAllChatSessionsWithFilters(Pageable pageable, String participantName, Constants.ConversationTypeEnum typeEnum, Constants.ConversationStatusEnum status) {
        // Trim participantName to avoid issues with whitespace
        String trimmedName = (participantName != null && !participantName.trim().isEmpty())
                ? participantName.trim()
                : null;

        // Convert enums to ordinal integers for native query (database stores SMALLINT)
        Integer typeOrdinal = typeEnum != null ? typeEnum.ordinal() : null;
        Integer statusOrdinal = status != null ? status.ordinal() : null;

        // Fetch all matching conversations (no pagination) so we can sort by latest message time
        // We ignore the sort parameters from controller and sort by latest message time instead
        List<Conversation> allConversations = conversationRepository.findAllWithFilters(
                trimmedName,
                typeOrdinal,
                statusOrdinal
        );

        // Build map of conversationId -> latest message createdAt using batch query
        Map<UUID, LocalDateTime> latestMessageMap = new HashMap<>();
        if (!allConversations.isEmpty()) {
            List<UUID> convIds = allConversations.stream().map(Conversation::getId).toList();
            List<Object[]> pairs = messageRepository.findLatestMessageCreatedAtByConversationIds(convIds);
            for (Object[] pair : pairs) {
                UUID id = (UUID) pair[0];
                LocalDateTime ts = (LocalDateTime) pair[1];
                latestMessageMap.put(id, ts);
            }
        }

        // Sort by latest message time descending, nulls (no messages) go last. Tie-breaker: updatedAt desc, then id.
        allConversations.sort((c1, c2) -> {
            LocalDateTime t1 = latestMessageMap.get(c1.getId());
            LocalDateTime t2 = latestMessageMap.get(c2.getId());

            if (t1 == null && t2 == null) {
                if (c1.getUpdatedAt() == null && c2.getUpdatedAt() == null) {
                    return c2.getId().compareTo(c1.getId());
                }
                if (c1.getUpdatedAt() == null) return 1;
                if (c2.getUpdatedAt() == null) return -1;
                return c2.getUpdatedAt().compareTo(c1.getUpdatedAt());
            }
            if (t1 == null) return 1;
            if (t2 == null) return -1;

            int cmp = t2.compareTo(t1);
            if (cmp != 0) return cmp;

            if (c1.getUpdatedAt() == null && c2.getUpdatedAt() == null) {
                return c2.getId().compareTo(c1.getId());
            }
            if (c1.getUpdatedAt() == null) return 1;
            if (c2.getUpdatedAt() == null) return -1;
            return c2.getUpdatedAt().compareTo(c1.getUpdatedAt());
        });

        // Manual pagination on sorted list (ignore sort from pageable)
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), allConversations.size());
        List<Conversation> paged = start <= end ? allConversations.subList(start, end) : Collections.emptyList();

        // Create unsorted pageable to avoid confusion
        Pageable unsortedPageable = org.springframework.data.domain.PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize()
        );
        Page<Conversation> conversationPage = new PageImpl<>(paged, unsortedPageable, allConversations.size());

        log.info("Filtered conversations: participantName={}, type={}, status={}, totalResults={}",
                trimmedName, typeEnum, status, conversationPage.getTotalElements());

        return conversationPage.map(conv -> conversationMapper.mapTo(conv, ConversationResponse.class));
    }

    /**
     * Convert Pageable sort field names from camelCase to snake_case for native SQL queries
     */
    private Pageable convertPageableToSnakeCase(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            return pageable;
        }

        org.springframework.data.domain.Sort newSort = org.springframework.data.domain.Sort.by(
                pageable.getSort().stream()
                        .map(order -> new org.springframework.data.domain.Sort.Order(
                                order.getDirection(),
                                camelToSnakeCase(order.getProperty())
                        ))
                        .collect(java.util.stream.Collectors.toList())
        );

        return org.springframework.data.domain.PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                newSort
        );
    }

    /**
     * Convert camelCase string to snake_case
     */
    private String camelToSnakeCase(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
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

        // Cancel booking if exists
        Booking booking = conversation.getBooking();
        if (booking != null) {
            booking.setStatus(Constants.BookingStatusEnum.CANCELED);
            bookingRepository.save(booking);
            log.info("Session canceled due to late join: conversation={}, booking={}, canceledBy={}",
                    conversationId, booking.getId(), canceledBy);
        } else {
            log.warn("Session canceled due to late join but no booking found: conversation={}, canceledBy={}",
                    conversationId, canceledBy);
        }
    }

    @Override
    @Transactional
    public void activateWaitingConversation(UUID conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));

        // Validate current status is WAITING
        if (!conversation.getStatus().equals(Constants.ConversationStatusEnum.WAITING)) {
            log.warn("Cannot activate conversation {} - current status is not WAITING: {}",
                    conversationId, conversation.getStatus());
            return;
        }

        // Activate conversation
        conversation.setStatus(Constants.ConversationStatusEnum.ACTIVE);
        conversationRepository.save(conversation);

        log.info("Conversation activated: conversationId={}, sessionStartTime={}",
                conversationId, conversation.getSessionStartTime());
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

        // Complete booking if exists
        Booking booking = conversation.getBooking();
        if (booking != null) {
            booking.setStatus(Constants.BookingStatusEnum.COMPLETED);
            bookingRepository.save(booking);
            log.info("Session auto-ended: conversation={}, booking={}",
                    conversationId, booking.getId());
        } else {
            log.warn("Session auto-ended but no booking found: conversation={}",
                    conversationId);
        }
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

    @Override
    @Transactional(readOnly = true)
    public ConversationStatisticResponse getConversationStatistics() {
        ConversationStatisticResponse conversationStatisticResponse = new ConversationStatisticResponse();

        int bookings = 0;
        int supports = 0;
        int admins = 0;
        int actives = 0;
        long totalMessages = messageRepository.count();

        List<Conversation> conversations = conversationRepository.findAll();
        for (Conversation conversation : conversations) {
            switch (conversation.getType()) {
                case ADMIN_CHAT ->  admins++;
                case SUPPORT -> supports++;
                case BOOKING_SESSION -> bookings++;
            }
            if (conversation.getStatus() == Constants.ConversationStatusEnum.ACTIVE) {
                actives++;
            }
        }

        conversationStatisticResponse.setSupportConversations(supports);
        conversationStatisticResponse.setAdminConversations(admins);
        conversationStatisticResponse.setBookingConversations(bookings);
        conversationStatisticResponse.setTotalMessages(totalMessages);
        conversationStatisticResponse.setTotalActives(actives);

        return conversationStatisticResponse;
    }

    @Override
    @Transactional
    public int fixAdminChatConversations() {
        log.info("Starting to fix ADMIN_CHAT conversations...");

        // Find all ADMIN_CHAT conversations that are not ACTIVE
        List<Conversation> adminChats = conversationRepository.findAll().stream()
                .filter(c -> c.getType() == Constants.ConversationTypeEnum.ADMIN_CHAT)
                .filter(c -> c.getStatus() != Constants.ConversationStatusEnum.ACTIVE)
                .toList();

        log.info("Found {} ADMIN_CHAT conversations with incorrect status", adminChats.size());

        int fixed = 0;
        for (Conversation conversation : adminChats) {
            log.info("Fixing conversation ID: {} from status {} to ACTIVE",
                    conversation.getId(), conversation.getStatus());

            conversation.setStatus(Constants.ConversationStatusEnum.ACTIVE);
            conversation.setSessionEndTime(null);  // ADMIN_CHAT should not have end time
            conversation.setCanceledBy(null);  // Clear canceled by field
            conversationRepository.save(conversation);
            fixed++;
        }

        log.info("Fixed {} ADMIN_CHAT conversations", fixed);
        return fixed;
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
