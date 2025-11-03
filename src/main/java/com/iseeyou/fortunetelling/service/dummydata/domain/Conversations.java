package com.iseeyou.fortunetelling.service.dummydata.domain;

import com.iseeyou.fortunetelling.entity.booking.Booking;
import com.iseeyou.fortunetelling.entity.chat.Conversation;
import com.iseeyou.fortunetelling.entity.chat.Message;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.repository.booking.BookingRepository;
import com.iseeyou.fortunetelling.repository.chat.ConversationRepository;
import com.iseeyou.fortunetelling.repository.chat.MessageRepository;
import com.iseeyou.fortunetelling.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@RequiredArgsConstructor
@Service
public class Conversations {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final BookingRepository bookingRepository;
    private final com.iseeyou.fortunetelling.repository.user.UserRepository userRepository;

    private final Random random = new Random();

    @Transactional
    public void createDummyData() {
        log.info("Bắt đầu tạo dummy data cho Conversations...");

        // Get confirmed bookings that don't have conversations yet
        List<Booking> confirmedBookings = bookingRepository.findAll().stream()
                .filter(booking -> booking.getStatus() == Constants.BookingStatusEnum.CONFIRMED)
                .filter(booking -> conversationRepository.findByBookingId(booking.getId()).isEmpty())
                .toList();

        if (confirmedBookings.isEmpty()) {
            log.warn("Không có confirmed booking nào để tạo conversation");
            return;
        }

        createConversationsWithStatusDistribution(confirmedBookings);

        // Create admin conversations
        createAdminConversations();

        log.info("Hoàn thành tạo dummy data cho Conversations.");
    }

    @Transactional
    public void recreateAllConversations() {
        log.warn("=== Xóa tất cả conversations và messages để tạo lại ===");

        // Delete all messages first (foreign key constraint)
        long deletedMessages = messageRepository.count();
        messageRepository.deleteAll();
        log.info("Đã xóa {} messages", deletedMessages);

        // Delete all conversations
        long deletedConversations = conversationRepository.count();
        conversationRepository.deleteAll();
        log.info("Đã xóa {} conversations", deletedConversations);

        // Get all confirmed bookings
        List<Booking> confirmedBookings = bookingRepository.findAll().stream()
                .filter(booking -> booking.getStatus() == Constants.BookingStatusEnum.CONFIRMED)
                .toList();

        if (confirmedBookings.isEmpty()) {
            log.warn("Không có confirmed booking nào để tạo conversation");
            return;
        }

        log.info("Tìm thấy {} confirmed bookings để tạo conversations mới", confirmedBookings.size());

        // Create conversations with proper status distribution
        createConversationsWithStatusDistribution(confirmedBookings);

        // Create admin conversations
        createAdminConversations();

        log.info("=== Hoàn thành tạo lại tất cả conversations ===");
    }

    private void createConversationsWithStatusDistribution(List<Booking> confirmedBookings) {
        log.info("Tạo {} conversations với phân phối status hợp lý", Math.min(confirmedBookings.size(), 15));

        int activeCount = 0;
        int endedCount = 0;
        int cancelledCount = 0;
        int waitingCount = 0;

        for (int i = 0; i < Math.min(confirmedBookings.size(), 15); i++) {
            Booking booking = confirmedBookings.get(i);
            Constants.ConversationStatusEnum status;

            // Distribute conversation statuses with more variety
            if (waitingCount < 2) {
                status = Constants.ConversationStatusEnum.WAITING;
                waitingCount++;
            } else if (activeCount < 3) {
                status = Constants.ConversationStatusEnum.ACTIVE;
                activeCount++;
            } else if (endedCount < 6) {
                status = Constants.ConversationStatusEnum.ENDED;
                endedCount++;
            } else if (cancelledCount < 4) {
                status = Constants.ConversationStatusEnum.CANCELLED;
                cancelledCount++;
            } else {
                break;
            }

            createConversationWithMessages(booking, status);
        }
        log.info("✅ Đã tạo {} WAITING, {} ACTIVE, {} ENDED, {} CANCELLED booking conversations",
                waitingCount, activeCount, endedCount, cancelledCount);
    }

    @Transactional
    public void createAdminConversations() {
        log.info("Bắt đầu tạo admin conversations...");

        // 1. Get admin user and create conversations with random customers/seers
        User admin = userRepository.findAll().stream()
                .filter(user -> user.getRole() == Constants.RoleEnum.ADMIN)
                .findFirst()
                .orElse(null);

        int adminConversationsCount = 0;

        if (admin != null) {
            // Get all customers and seers (exclude admin)
            List<User> targetUsers = userRepository.findAll().stream()
                    .filter(user -> user.getRole() == Constants.RoleEnum.CUSTOMER ||
                                   user.getRole() == Constants.RoleEnum.SEER)
                    .toList();

            if (!targetUsers.isEmpty()) {
                log.info("Tìm thấy {} users (customers & seers) để tạo admin conversations", targetUsers.size());

                // Create admin conversations with random users (5-10 conversations)
                int numberOfConversations = Math.min(targetUsers.size(), 5 + random.nextInt(6));

                for (int i = 0; i < numberOfConversations; i++) {
                    User targetUser = targetUsers.get(i);

                    // Check if admin conversation already exists
                    if (conversationRepository.findAdminConversationByAdminAndTarget(admin.getId(), targetUser.getId()).isPresent()) {
                        log.info("Admin conversation đã tồn tại với user {}", targetUser.getId());
                        continue;
                    }

                    createAdminConversationWithMessages(admin, targetUser);
                    adminConversationsCount++;
                }

                log.info("Đã tạo {} admin conversations từ ADMIN", adminConversationsCount);
            }
        } else {
            log.warn("Không tìm thấy admin user để tạo admin conversations");
        }

        // 2. Create conversations from customers to 5 random seers each
        log.info("=== Bắt đầu tạo conversations giữa CUSTOMERS và SEERS ===");

        List<User> customers = userRepository.findAll().stream()
                .filter(user -> user.getRole() == Constants.RoleEnum.CUSTOMER)
                .toList();

        List<User> seers = userRepository.findAll().stream()
                .filter(user -> user.getRole() == Constants.RoleEnum.SEER)
                .toList();

        log.info("Tìm thấy {} customers và {} seers", customers.size(), seers.size());

        if (customers.isEmpty()) {
            log.warn("⚠️ Không có customers nào để tạo customer-seer conversations");
            return;
        }

        if (seers.isEmpty()) {
            log.warn("⚠️ Không có seers nào để tạo customer-seer conversations");
            return;
        }

        int customerConversationsCount = 0;
        int skippedCount = 0;

        for (User customer : customers) {
            // Each customer creates conversations with 5 random seers
            List<User> shuffledSeers = new ArrayList<>(seers);
            java.util.Collections.shuffle(shuffledSeers, random);

            int numberOfSeers = Math.min(5, shuffledSeers.size());

            log.info("Customer {} sẽ tạo conversations với {} seers", customer.getFullName(), numberOfSeers);

            for (int i = 0; i < numberOfSeers; i++) {
                User seer = shuffledSeers.get(i);

                // Check if conversation already exists
                if (conversationRepository.findAdminConversationByAdminAndTarget(customer.getId(), seer.getId()).isPresent()) {
                    log.debug("Conversation đã tồn tại giữa customer {} và seer {}",
                            customer.getId(), seer.getId());
                    skippedCount++;
                    continue;
                }

                try {
                    createCustomerSeerConversation(customer, seer);
                    customerConversationsCount++;
                    log.info("✅ Đã tạo conversation #{} giữa customer {} và seer {}",
                            customerConversationsCount, customer.getFullName(), seer.getFullName());
                } catch (Exception e) {
                    log.error("❌ Lỗi khi tạo conversation giữa customer {} và seer {}: {}",
                            customer.getId(), seer.getId(), e.getMessage(), e);
                }
            }
        }

        log.info("✅ Đã tạo {} customer-seer conversations (bỏ qua {} đã tồn tại)",
                customerConversationsCount, skippedCount);
        log.info("=== Tổng số admin conversations đã tạo: {} ===",
                adminConversationsCount + customerConversationsCount);
    }

    private void createCustomerSeerConversation(User customer, User seer) {
        log.debug("Creating conversation: Customer {} ({}) -> Seer {} ({})",
                customer.getFullName(), customer.getId(),
                seer.getFullName(), seer.getId());

        // Create conversation from customer to seer (customer acts as "admin" initiator)
        Conversation conversation = new Conversation();
        conversation.setBooking(null);  // No booking for admin chat
        conversation.setType(Constants.ConversationTypeEnum.ADMIN_CHAT);
        conversation.setAdmin(customer);  // Customer is the initiator
        conversation.setTargetUser(seer);  // Seer is the target
        conversation.setSessionStartTime(LocalDateTime.now().minusDays(random.nextInt(7) + 1)); // 1-7 days ago
        conversation.setSessionEndTime(null);  // No end time for admin chat
        conversation.setSessionDurationMinutes(null);  // No duration limit
        conversation.setStatus(Constants.ConversationStatusEnum.ACTIVE);

        Conversation savedConversation = conversationRepository.save(conversation);
        log.debug("Saved conversation with ID: {}", savedConversation.getId());

        // Create messages between customer and seer
        createCustomerSeerMessages(savedConversation, customer, seer);
    }

    private void createCustomerSeerMessages(Conversation conversation, User customer, User seer) {
        log.debug("Creating messages for conversation {} between customer {} and seer {}",
                conversation.getId(), customer.getFullName(), seer.getFullName());

        List<Message> messages = new ArrayList<>();
        LocalDateTime conversationStart = conversation.getSessionStartTime();

        // Customer initiates conversation with seer
        String[][] dialogue = new String[][]{
                {"Xin chào, tôi muốn tư vấn về dịch vụ của bạn",
                 "Chào bạn! Cảm ơn bạn đã quan tâm. Tôi có thể giúp gì cho bạn?"},
                {"Tôi muốn biết về dịch vụ xem bói tarot của bạn",
                 "Tôi chuyên về tarot và có thể tư vấn về tình yêu, sự nghiệp, tài chính"},
                {"Phí tư vấn là bao nhiêu và kéo dài bao lâu?",
                 "Phí từ 100k-500k tùy gói, thời gian từ 15-60 phút. Bạn quan tâm gói nào?"},
                {"Tôi muốn gói 30 phút, có thể đặt lịch được không?",
                 "Được ạ! Bạn có thể đặt lịch qua hệ thống. Khi nào tiện cho bạn?"},
                {"Cảm ơn bạn nhiều, tôi sẽ đặt lịch sớm",
                 "Rất vui được hỗ trợ! Hẹn gặp lại bạn"},
        };

        // Create messages with timing
        for (int i = 0; i < dialogue.length; i++) {
            // Customer message
            messages.add(createMessage(
                    conversation,
                    customer,
                    dialogue[i][0],
                    conversationStart.plusMinutes(i * 5),
                    Constants.MessageStatusEnum.READ
            ));

            // Seer response
            messages.add(createMessage(
                    conversation,
                    seer,
                    dialogue[i][1],
                    conversationStart.plusMinutes(i * 5 + 2),
                    i == dialogue.length - 1 ? Constants.MessageStatusEnum.UNREAD : Constants.MessageStatusEnum.READ
            ));
        }

        messageRepository.saveAll(messages);
        log.info("Đã tạo {} messages cho customer-seer conversation {}", messages.size(), conversation.getId());
    }

    private void createAdminConversationWithMessages(User admin, User targetUser) {
        // Create admin conversation
        Conversation conversation = new Conversation();
        conversation.setBooking(null);  // No booking for admin chat
        conversation.setType(Constants.ConversationTypeEnum.ADMIN_CHAT);
        conversation.setAdmin(admin);
        conversation.setTargetUser(targetUser);
        conversation.setSessionStartTime(LocalDateTime.now().minusDays(random.nextInt(7) + 1)); // 1-7 days ago
        conversation.setSessionEndTime(null);  // No end time for admin chat
        conversation.setSessionDurationMinutes(null);  // No duration limit
        conversation.setStatus(Constants.ConversationStatusEnum.ACTIVE);

        conversationRepository.save(conversation);

        log.info("Tạo ADMIN_CHAT conversation giữa admin {} và {} {}",
                admin.getId(), targetUser.getRole(), targetUser.getId());

        // Create messages for admin conversation
        createAdminConversationMessages(conversation, admin, targetUser);
    }

    private void createAdminConversationMessages(Conversation conversation, User admin, User targetUser) {
        List<Message> messages = new ArrayList<>();
        LocalDateTime conversationStart = conversation.getSessionStartTime();

        // Messages depend on target user role
        String[][] dialogue;

        if (targetUser.getRole() == Constants.RoleEnum.CUSTOMER) {
            dialogue = new String[][]{
                    {"Xin chào, tôi là quản trị viên của hệ thống", "Chào admin ạ"},
                    {"Tôi thấy bạn có một số thắc mắc về dịch vụ. Tôi có thể giúp gì cho bạn?",
                     "Em muốn hỏi về quy trình hoàn tiền ạ"},
                    {"Quy trình hoàn tiền sẽ được xử lý trong vòng 5-7 ngày làm việc. Bạn đã hủy booking nào chưa?",
                     "Em đã hủy booking nhưng chưa thấy tiền hoàn lại"},
                    {"Để tôi kiểm tra. Bạn vui lòng cung cấp mã booking nhé",
                     "Mã booking là #BK123456 ạ"},
                    {"Cảm ơn bạn. Tôi sẽ kiểm tra và phản hồi trong 24h. Có vấn đề gì khác không?",
                     "Không ạ, cảm ơn admin nhiều!"},
            };
        } else { // SEER
            dialogue = new String[][]{
                    {"Xin chào, tôi là quản trị viên của hệ thống", "Chào admin"},
                    {"Tôi muốn thông báo về một số cập nhật mới trong hệ thống",
                     "Vâng, tôi lắng nghe ạ"},
                    {"Chúng tôi vừa thêm tính năng gia hạn phiên chat. Bạn có thể gia hạn thêm 15-30 phút nếu khách hàng yêu cầu",
                     "Điều đó thật tuyệt! Cảm ơn admin đã thông báo"},
                    {"Ngoài ra, vui lòng cập nhật lịch trống của bạn thường xuyên để khách hàng có thể đặt lịch dễ dàng hơn",
                     "Vâng, tôi sẽ cập nhật ngay hôm nay"},
                    {"Rất tốt! Nếu có vấn đề gì cứ liên hệ với tôi nhé",
                     "Cảm ơn admin!"},
            };
        }

        // Create messages with timing
        for (int i = 0; i < dialogue.length; i++) {
            // Admin message
            messages.add(createMessage(
                    conversation,
                    admin,
                    dialogue[i][0],
                    conversationStart.plusMinutes(i * 5),
                    Constants.MessageStatusEnum.READ
            ));

            // Target user response
            messages.add(createMessage(
                    conversation,
                    targetUser,
                    dialogue[i][1],
                    conversationStart.plusMinutes(i * 5 + 2),
                    i == dialogue.length - 1 ? Constants.MessageStatusEnum.UNREAD : Constants.MessageStatusEnum.READ
            ));
        }

        messageRepository.saveAll(messages);
        log.info("Đã tạo {} messages cho admin conversation {}", messages.size(), conversation.getId());
    }


    private void createConversationWithMessages(Booking booking, Constants.ConversationStatusEnum status) {
        User customer = booking.getCustomer();
        User seer = booking.getServicePackage().getSeer();

        Conversation conversation = new Conversation();
        conversation.setBooking(booking);
        conversation.setType(Constants.ConversationTypeEnum.BOOKING_SESSION);
        conversation.setStatus(status);
        conversation.setSessionDurationMinutes(60); // Default 60 minutes

        LocalDateTime sessionStart;
        LocalDateTime sessionEnd;
        LocalDateTime customerJoined;
        LocalDateTime seerJoined;

        switch (status) {
            case WAITING -> {
                // Waiting conversation - scheduled to start in the future
                int minutesUntilStart = 15 + random.nextInt(46); // 15-60 minutes in future
                sessionStart = LocalDateTime.now().plusMinutes(minutesUntilStart);
                sessionEnd = sessionStart.plusMinutes(60);
                customerJoined = null; // Not joined yet
                seerJoined = null; // Not joined yet

                conversation.setSessionStartTime(sessionStart);
                conversation.setSessionEndTime(sessionEnd);
                conversation.setCustomerJoinedAt(null);
                conversation.setSeerJoinedAt(null);
                conversation.setWarningNotificationSent(false);
                conversation.setExtendedMinutes(0);

                log.info("Tạo WAITING conversation cho booking {} (will start in {} mins)",
                        booking.getId(), minutesUntilStart);
            }
            case ACTIVE -> {
                // Active conversation - started 5-15 minutes ago, still has time remaining
                int minutesAgo = 5 + random.nextInt(11);
                sessionStart = LocalDateTime.now().minusMinutes(minutesAgo);
                // Session will end in the FUTURE (30-50 minutes from now)
                sessionEnd = LocalDateTime.now().plusMinutes(30 + random.nextInt(21));
                customerJoined = sessionStart;
                seerJoined = sessionStart.plusMinutes(1);

                conversation.setSessionStartTime(sessionStart);
                conversation.setSessionEndTime(sessionEnd);
                conversation.setCustomerJoinedAt(customerJoined);
                conversation.setSeerJoinedAt(seerJoined);
                conversation.setWarningNotificationSent(false);
                conversation.setExtendedMinutes(0);

                log.info("Tạo ACTIVE conversation cho booking {} (started {} mins ago, will end in future)",
                        booking.getId(), minutesAgo);
            }
            case ENDED -> {
                // Ended conversation - completed 1-7 days ago
                int daysAgo = 1 + random.nextInt(7);
                sessionStart = LocalDateTime.now().minusDays(daysAgo).minusHours(1);
                sessionEnd = sessionStart.plusMinutes(60);
                customerJoined = sessionStart;
                seerJoined = sessionStart.plusMinutes(1);

                conversation.setSessionStartTime(sessionStart);
                conversation.setSessionEndTime(sessionEnd);
                conversation.setCustomerJoinedAt(customerJoined);
                conversation.setSeerJoinedAt(seerJoined);
                conversation.setWarningNotificationSent(true);
                conversation.setExtendedMinutes(0);

                log.info("Tạo ENDED conversation cho booking {} (completed {} days ago)",
                        booking.getId(), daysAgo);
            }
            case CANCELLED -> {
                // Cancelled conversation - 2-5 days ago
                int daysAgo = 2 + random.nextInt(4);
                sessionStart = LocalDateTime.now().minusDays(daysAgo);
                sessionEnd = sessionStart.plusMinutes(60);

                boolean cancelledByCustomer = random.nextBoolean();
                if (cancelledByCustomer) {
                    customerJoined = sessionStart;
                    seerJoined = null; // Seer never joined
                    conversation.setCanceledBy(Constants.RoleEnum.CUSTOMER.name());
                } else {
                    customerJoined = sessionStart;
                    seerJoined = sessionStart.plusMinutes(1);
                    conversation.setCanceledBy(Constants.RoleEnum.SEER.name());
                }

                conversation.setSessionStartTime(sessionStart);
                conversation.setSessionEndTime(sessionEnd);
                conversation.setCustomerJoinedAt(customerJoined);
                conversation.setSeerJoinedAt(seerJoined);
                conversation.setWarningNotificationSent(false);
                conversation.setExtendedMinutes(0);

                log.info("Tạo CANCELLED conversation cho booking {} (cancelled by {} {} days ago)",
                        booking.getId(), conversation.getCanceledBy(), daysAgo);
            }
        }

        conversationRepository.save(conversation);

        // Create messages for this conversation
        createMessagesForConversation(conversation, customer, seer);
    }

    private void createMessagesForConversation(Conversation conversation, User customer, User seer) {
        List<Message> messages = new ArrayList<>();
        LocalDateTime sessionStart = conversation.getSessionStartTime();

        switch (conversation.getStatus()) {
            case WAITING -> {
                // No messages yet for WAITING conversations
                log.info("WAITING conversation {} - no messages created yet", conversation.getId());
                return;
            }
            case ACTIVE -> messages.addAll(createActiveConversationMessages(conversation, customer, seer, sessionStart));
            case ENDED -> messages.addAll(createEndedConversationMessages(conversation, customer, seer, sessionStart));
            case CANCELLED -> messages.addAll(createCancelledConversationMessages(conversation, customer, seer, sessionStart));
        }

        messageRepository.saveAll(messages);
        log.info("Đã tạo {} messages cho conversation {}", messages.size(), conversation.getId());
    }

    private List<Message> createActiveConversationMessages(Conversation conversation, User customer, User seer, LocalDateTime start) {
        List<Message> messages = new ArrayList<>();

        String[][] customerMessages = {
                {"Xin chào thầy/chị!", "Chào bạn! Tôi đã sẵn sàng. Bạn muốn hỏi về vấn đề gì?"},
                {"Em muốn xem bói về tình yêu ạ", "Được, hãy cho tôi biết thêm về tình trạng hiện tại của bạn"},
                {"Em đang độc thân và muốn biết khi nào gặp được người phù hợp", "Để tôi xem lá bài cho bạn nhé"},
                {"Dạ, em cảm ơn thầy/chị ạ", null},
        };

        for (int i = 0; i < customerMessages.length; i++) {
            // Customer message
            Message customerMsg = createMessage(
                    conversation,
                    customer,
                    customerMessages[i][0],
                    start.plusMinutes(i * 2),
                    i < customerMessages.length - 1 ? Constants.MessageStatusEnum.READ : Constants.MessageStatusEnum.UNREAD
            );
            messages.add(customerMsg);

            // Seer response (if exists)
            if (customerMessages[i][1] != null) {
                Message seerMsg = createMessage(
                        conversation,
                        seer,
                        customerMessages[i][1],
                        start.plusMinutes(i * 2 + 1),
                        i < customerMessages.length - 2 ? Constants.MessageStatusEnum.READ : Constants.MessageStatusEnum.UNREAD
                );
                messages.add(seerMsg);
            }
        }

        return messages;
    }

    private List<Message> createEndedConversationMessages(Conversation conversation, User customer, User seer, LocalDateTime start) {
        List<Message> messages = new ArrayList<>();

        String[][] dialogue = {
                {"Chào anh/chị, em muốn hỏi về công việc", "Chào em! Hãy cho anh/chị biết chi tiết tình hình công việc của em"},
                {"Em đang làm marketing, muốn biết có nên chuyển công ty không", "Để anh/chị xem lá bài tarot cho em"},
                {"Dạ, em cảm ơn ạ", "Theo lá bài, em nên ở lại công ty hiện tại. Tháng sau sẽ có cơ hội thăng tiến"},
                {"Em sẽ cố gắng ạ", "Chúc em thành công! Nếu có gì khó khăn cứ liên hệ lại nhé"},
                {"Cảm ơn thầy/chị nhiều ạ!", "Không có gì. Chúc em một ngày tốt lành!"},
        };

        for (int i = 0; i < dialogue.length; i++) {
            // Customer message
            messages.add(createMessage(
                    conversation,
                    customer,
                    dialogue[i][0],
                    start.plusMinutes(i * 10),
                    Constants.MessageStatusEnum.READ
            ));

            // Seer response
            messages.add(createMessage(
                    conversation,
                    seer,
                    dialogue[i][1],
                    start.plusMinutes(i * 10 + 3),
                    Constants.MessageStatusEnum.READ
            ));
        }

        // System message - session ended
        messages.add(createSystemMessage(
                conversation,
                "Phiên chat đã kết thúc. Cảm ơn bạn đã sử dụng dịch vụ!",
                conversation.getSessionEndTime()
        ));

        return messages;
    }

    private List<Message> createCancelledConversationMessages(Conversation conversation, User customer, User seer, LocalDateTime start) {
        List<Message> messages = new ArrayList<>();

        // Customer greeting
        messages.add(createMessage(
                conversation,
                customer,
                "Xin chào!",
                start,
                Constants.MessageStatusEnum.READ
        ));

        if (conversation.getSeerJoinedAt() != null) {
            // Seer joined - add one more message before cancellation
            messages.add(createMessage(
                    conversation,
                    seer,
                    "Xin chào bạn!",
                    start.plusMinutes(1),
                    Constants.MessageStatusEnum.READ
            ));
        }

        // System message - cancellation
        String cancelMessage = conversation.getCanceledBy().equals(Constants.RoleEnum.CUSTOMER.name())
                ? "Khách hàng đã hủy phiên chat"
                : "Seer đã hủy phiên chat do lý do bận đột xuất";

        messages.add(createSystemMessage(
                conversation,
                cancelMessage,
                start.plusMinutes(5)
        ));

        return messages;
    }

    private Message createMessage(Conversation conversation, User sender, String content,
                                   LocalDateTime createdAt, Constants.MessageStatusEnum status) {
        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setTextContent(content);
        message.setMessageType(Constants.MessageTypeEnum.USER.getValue());
        message.setStatus(status);
        message.setCreatedAt(createdAt);
        message.setUpdatedAt(createdAt);
        return message;
    }

    private Message createSystemMessage(Conversation conversation, String content, LocalDateTime createdAt) {
        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(null); // System message has no sender
        message.setTextContent(content);
        message.setMessageType(Constants.MessageTypeEnum.SYSTEM.getValue());
        message.setStatus(Constants.MessageStatusEnum.READ);
        message.setCreatedAt(createdAt);
        message.setUpdatedAt(createdAt);
        return message;
    }
}

