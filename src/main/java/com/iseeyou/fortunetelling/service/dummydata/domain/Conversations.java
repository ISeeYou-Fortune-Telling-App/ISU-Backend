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

        log.info("Tìm thấy {} confirmed bookings, sẽ tạo conversations với các trạng thái khác nhau", confirmedBookings.size());

        int activeCount = 0;
        int endedCount = 0;
        int cancelledCount = 0;

        for (int i = 0; i < Math.min(confirmedBookings.size(), 10); i++) {
            Booking booking = confirmedBookings.get(i);
            Constants.ConversationStatusEnum status;

            // Distribute conversation statuses
            if (activeCount < 3) {
                status = Constants.ConversationStatusEnum.ACTIVE;
                activeCount++;
            } else if (endedCount < 4) {
                status = Constants.ConversationStatusEnum.ENDED;
                endedCount++;
            } else if (cancelledCount < 3) {
                status = Constants.ConversationStatusEnum.CANCELLED;
                cancelledCount++;
            } else {
                break;
            }

            createConversationWithMessages(booking, status);
        }
        log.info("Đã tạo {} ACTIVE, {} ENDED, {} CANCELLED booking conversations",
                activeCount, endedCount, cancelledCount);

        // Create admin conversations
        createAdminConversations();

        log.info("Hoàn thành tạo dummy data cho Conversations.");
    }

    @Transactional
    public void createAdminConversations() {
        log.info("Bắt đầu tạo admin conversations...");

        // Get admin user
        User admin = userRepository.findAll().stream()
                .filter(user -> user.getRole() == Constants.RoleEnum.ADMIN)
                .findFirst()
                .orElse(null);

        if (admin == null) {
            log.warn("Không tìm thấy admin user để tạo admin conversations");
            return;
        }

        // Get all customers and seers (exclude admin)
        List<User> targetUsers = userRepository.findAll().stream()
                .filter(user -> user.getRole() == Constants.RoleEnum.CUSTOMER ||
                               user.getRole() == Constants.RoleEnum.SEER)
                .toList();

        if (targetUsers.isEmpty()) {
            log.warn("Không tìm thấy users nào để tạo admin conversations");
            return;
        }

        log.info("Tìm thấy {} users (customers & seers) để tạo admin conversations", targetUsers.size());

        int createdCount = 0;
        // Create admin conversations with random users (not all)
        int numberOfConversations = Math.min(targetUsers.size(), 5 + random.nextInt(6)); // 5-10 conversations

        for (int i = 0; i < numberOfConversations; i++) {
            User targetUser = targetUsers.get(i);

            // Check if admin conversation already exists
            if (conversationRepository.findAdminConversationByAdminAndTarget(admin.getId(), targetUser.getId()).isPresent()) {
                log.info("Admin conversation đã tồn tại với user {}", targetUser.getId());
                continue;
            }

            createAdminConversationWithMessages(admin, targetUser);
            createdCount++;
        }

        log.info("Đã tạo {} admin conversations", createdCount);
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
            case ACTIVE -> {
                // Active conversation - started 5-15 minutes ago
                int minutesAgo = 5 + random.nextInt(11);
                sessionStart = LocalDateTime.now().minusMinutes(minutesAgo);
                sessionEnd = sessionStart.plusMinutes(60);
                customerJoined = sessionStart;
                seerJoined = sessionStart.plusMinutes(1);

                conversation.setSessionStartTime(sessionStart);
                conversation.setSessionEndTime(sessionEnd);
                conversation.setCustomerJoinedAt(customerJoined);
                conversation.setSeerJoinedAt(seerJoined);
                conversation.setWarningNotificationSent(false);
                conversation.setExtendedMinutes(0);

                log.info("Tạo ACTIVE conversation cho booking {} (started {} mins ago)",
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

