package com.iseeyou.fortunetelling.service.report.impl;

import com.iseeyou.fortunetelling.dto.request.chat.session.ChatMessageRequest;
import com.iseeyou.fortunetelling.dto.request.report.ReportViolationActionRequest;
import com.iseeyou.fortunetelling.entity.chat.Conversation;
import com.iseeyou.fortunetelling.entity.report.Report;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.exception.NotFoundException;
import com.iseeyou.fortunetelling.repository.chat.ConversationRepository;
import com.iseeyou.fortunetelling.repository.report.ReportRepository;
import com.iseeyou.fortunetelling.repository.user.UserRepository;
import com.iseeyou.fortunetelling.service.chat.ConversationService;
import com.iseeyou.fortunetelling.service.chat.MessageService;
import com.iseeyou.fortunetelling.service.notification.PushNotificationService;
import com.iseeyou.fortunetelling.service.report.ReportViolationService;
import com.iseeyou.fortunetelling.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportViolationServiceImpl implements ReportViolationService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationService conversationService;
    private final MessageService messageService;
    private final JavaMailSender mailSender;
    private final PushNotificationService pushNotificationService;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.name}")
    private String appName;

    @Override
    @Transactional
    public Report handleViolationAction(UUID reportId, ReportViolationActionRequest request) {
        log.info("Processing violation action {} for report {}", request.getAction(), reportId);

        // Lấy report
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("Report not found with id: " + reportId));

        User reportedUser = report.getReportedUser();
        User reporter = report.getReporter();

        // Xử lý theo action
        switch (request.getAction()) {
            case WARNING:
                handleWarningAction(report, reportedUser, reporter, request.getDecisionReason());
                break;
            case SUSPEND:
                handleSuspendAction(report, reportedUser, reporter, request.getDecisionReason(), request.getSuspensionDays());
                break;
            case BAN:
                handleBanAction(report, reportedUser, reporter, request.getDecisionReason());
                break;
        }

        // Cập nhật report status và action
        report.setStatus(Constants.ReportStatusEnum.RESOLVED);
        report.setNote(request.getDecisionReason());

        return reportRepository.save(report);
    }

    private void handleWarningAction(Report report, User reportedUser, User reporter, String reason) {
        log.info("Issuing warning to user {}", reportedUser.getId());

        // Tăng warning count
        reportedUser.setWarningCount((reportedUser.getWarningCount() != null ? reportedUser.getWarningCount() : 0) + 1);
        userRepository.save(reportedUser);

        // Cập nhật report action
        report.setActionType(Constants.ReportActionEnum.WARNING_ISSUED);

        // Tạo hoặc lấy conversation với user bị báo cáo
        Conversation reportedUserConversation = getOrCreateAdminConversation(reportedUser);

        // Gửi tin nhắn cảnh báo đến người bị báo cáo
        String warningMessage = String.format(
                "⚠️ CẢNH BÁO VI PHẠM ⚠️\n\n" +
                "Bạn đã nhận được cảnh báo từ hệ thống do vi phạm quy định.\n\n" +
                "Lý do: %s\n\n" +
                "Đây là cảnh báo thứ %d. Vui lòng hoạt động đúng quy định và tích cực hơn để tránh bị đình chỉ hoặc khóa tài khoản.\n\n" +
                "Trân trọng,\n%s",
                reason,
                reportedUser.getWarningCount(),
                appName
        );

        sendAdminMessage(reportedUserConversation.getId(), warningMessage);

        // Gửi push notification cho người bị báo cáo (truyền cả recipientId và fcmToken)
        sendNotification(
                reportedUser.getId().toString(),
                reportedUser.getFcmToken(),
                "⚠️ Cảnh báo vi phạm",
                "Bạn đã nhận được cảnh báo từ hệ thống. Vui lòng kiểm tra tin nhắn."
        );

        // Tạo hoặc lấy conversation với người báo cáo
        Conversation reporterConversation = getOrCreateAdminConversation(reporter);

        // Gửi tin nhắn thông báo cho người báo cáo
        String reporterMessage = String.format(
                "✅ KẾT QUẢ XỬ LÝ BÁO CÁO\n\n" +
                "Báo cáo của bạn đã được xử lý.\n\n" +
                "Hành động: Đã cảnh báo người vi phạm\n" +
                "Lý do quyết định: %s\n\n" +
                "Cảm ơn bạn đã góp phần xây dựng cộng đồng lành mạnh!\n\n" +
                "Trân trọng,\n%s",
                reason,
                appName
        );

        sendAdminMessage(reporterConversation.getId(), reporterMessage);

        // Gửi push notification cho người báo cáo (truyền cả recipientId và fcmToken)
        sendNotification(
                reporter.getId().toString(),
                reporter.getFcmToken(),
                "✅ Báo cáo đã được xử lý",
                "Người vi phạm đã bị cảnh báo. Cảm ơn bạn đã báo cáo!"
        );

        log.info("Warning issued successfully to user {}", reportedUser.getId());
    }

    private void handleSuspendAction(Report report, User reportedUser, User reporter, String reason, Integer suspensionDays) {
        if (suspensionDays == null || suspensionDays <= 0) {
            throw new IllegalArgumentException("Suspension days must be greater than 0");
        }

        log.info("Suspending user {} for {} days", reportedUser.getId(), suspensionDays);

        // Đặt thời gian đình chỉ
        LocalDateTime suspendedUntil = LocalDateTime.now().plusDays(suspensionDays);
        reportedUser.setSuspendedUntil(suspendedUntil);
        reportedUser.setSuspensionReason(reason);
        reportedUser.setStatus(Constants.StatusProfileEnum.BLOCKED);
        userRepository.save(reportedUser);

        // Cập nhật report action
        report.setActionType(Constants.ReportActionEnum.USER_SUSPENDED);

        // Gửi email thông báo đình chỉ
        sendSuspensionEmail(reportedUser.getEmail(), reportedUser.getFullName(), reason, suspensionDays, suspendedUntil);

        // Tạo hoặc lấy conversation với người báo cáo
        Conversation reporterConversation = getOrCreateAdminConversation(reporter);

        // Gửi tin nhắn thông báo cho người báo cáo
        String reporterMessage = String.format(
                "✅ KẾT QUẢ XỬ LÝ BÁO CÁO\n\n" +
                "Báo cáo của bạn đã được xử lý.\n\n" +
                "Hành động: Đã đình chỉ tài khoản người vi phạm trong %d ngày\n" +
                "Lý do quyết định: %s\n" +
                "Thời gian đình chỉ đến: %s\n\n" +
                "Cảm ơn bạn đã góp phần xây dựng cộng đồng lành mạnh!\n\n" +
                "Trân trọng,\n%s",
                suspensionDays,
                reason,
                suspendedUntil,
                appName
        );

        sendAdminMessage(reporterConversation.getId(), reporterMessage);

        // Gửi push notification cho người báo cáo (truyền cả recipientId và fcmToken)
        sendNotification(
                reporter.getId().toString(),
                reporter.getFcmToken(),
                "✅ Báo cáo đã được xử lý",
                String.format("Tài khoản vi phạm đã bị đình chỉ %d ngày.", suspensionDays)
        );

        log.info("User {} suspended successfully until {}", reportedUser.getId(), suspendedUntil);
    }

    private void handleBanAction(Report report, User reportedUser, User reporter, String reason) {
        log.info("Banning user {}", reportedUser.getId());

        // Cấm vĩnh viễn
        reportedUser.setIsBanned(true);
        reportedUser.setBanReason(reason);
        reportedUser.setBannedAt(LocalDateTime.now());
        reportedUser.setStatus(Constants.StatusProfileEnum.BLOCKED);
        userRepository.save(reportedUser);

        // Cập nhật report action
        report.setActionType(Constants.ReportActionEnum.USER_BANNED);

        // Gửi email thông báo cấm tài khoản
        sendBanEmail(reportedUser.getEmail(), reportedUser.getFullName(), reason);

        // Tạo hoặc lấy conversation với người báo cáo
        Conversation reporterConversation = getOrCreateAdminConversation(reporter);

        // Gửi tin nhắn thông báo cho người báo cáo
        String reporterMessage = String.format(
                "✅ KẾT QUẢ XỬ LÝ BÁO CÁO\n\n" +
                "Báo cáo của bạn đã được xử lý.\n\n" +
                "Hành động: Đã cấm vĩnh viễn tài khoản người vi phạm\n" +
                "Lý do quyết định: %s\n\n" +
                "Cảm ơn bạn đã góp phần xây dựng cộng đồng lành mạnh!\n\n" +
                "Trân trọng,\n%s",
                reason,
                appName
        );

        sendAdminMessage(reporterConversation.getId(), reporterMessage);

        // Gửi push notification cho người báo cáo (truyền cả recipientId và fcmToken)
        sendNotification(
                reporter.getId().toString(),
                reporter.getFcmToken(),
                "✅ Báo cáo đã được xử lý",
                "Tài khoản vi phạm đã bị cấm vĩnh viễn."
        );

        log.info("User {} banned successfully", reportedUser.getId());
    }

    private Conversation getOrCreateAdminConversation(User user) {
        // Tìm admin user
        User admin = userRepository.findByRole(Constants.RoleEnum.ADMIN)
                .stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Admin user not found"));

        // Tìm conversation hiện có giữa admin và user
        Conversation conversation = conversationRepository.findAdminConversationWithUser(admin.getId(), user.getId())
                .orElse(null);

        // Nếu conversation tồn tại nhưng không ACTIVE, kích hoạt lại
        if (conversation != null) {
            if (!conversation.getStatus().equals(Constants.ConversationStatusEnum.ACTIVE)) {
                log.info("Reactivating admin conversation {} for user {}", conversation.getId(), user.getId());
                conversation.setStatus(Constants.ConversationStatusEnum.ACTIVE);
                conversationRepository.save(conversation);
            }
            return conversation;
        }

        // Tạo conversation mới nếu chưa có
        log.info("Creating new admin conversation for user {}", user.getId());
        conversationService.createAdminConversation(user.getId(), null);
        return conversationRepository.findAdminConversationWithUser(admin.getId(), user.getId())
                .orElseThrow(() -> new RuntimeException("Failed to create admin conversation"));
    }

    private void sendAdminMessage(UUID conversationId, String message) {
        // Tìm admin user
        User admin = userRepository.findByRole(Constants.RoleEnum.ADMIN)
                .stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Admin user not found"));

        ChatMessageRequest messageRequest = new ChatMessageRequest();
        messageRequest.setTextContent(message);

        messageService.sendMessage(conversationId, messageRequest, admin);
    }

    private void sendSuspensionEmail(String email, String fullName, String reason, Integer days, LocalDateTime suspendedUntil) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject(String.format("[%s] Thông báo đình chỉ tài khoản", appName));

            String emailContent = String.format(
                    "Kính gửi %s,\n\n" +
                    "Tài khoản của bạn đã bị đình chỉ do vi phạm quy định của hệ thống.\n\n" +
                    "Lý do: %s\n" +
                    "Thời gian đình chỉ: %d ngày\n" +
                    "Tài khoản sẽ được mở lại vào: %s\n\n" +
                    "Trong thời gian bị đình chỉ, bạn sẽ không thể sử dụng các tính năng của hệ thống.\n\n" +
                    "Nếu bạn cho rằng đây là nhầm lẫn, vui lòng liên hệ với chúng tôi để được hỗ trợ.\n\n" +
                    "Trân trọng,\n" +
                    "Đội ngũ %s",
                    fullName,
                    reason,
                    days,
                    suspendedUntil,
                    appName
            );

            message.setText(emailContent);
            mailSender.send(message);

            log.info("Suspension email sent successfully to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send suspension email to: {}", email, e);
        }
    }

    private void sendBanEmail(String email, String fullName, String reason) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject(String.format("[%s] Thông báo cấm tài khoản", appName));

            String emailContent = String.format(
                    "Kính gửi %s,\n\n" +
                    "Tài khoản của bạn đã bị cấm vĩnh viễn do vi phạm nghiêm trọng quy định của hệ thống.\n\n" +
                    "Lý do: %s\n\n" +
                    "Bạn sẽ không thể sử dụng tài khoản này nữa.\n\n" +
                    "Nếu bạn cho rằng đây là nhầm lẫn, vui lòng liên hệ với chúng tôi để được hỗ trợ và kháng cáo.\n\n" +
                    "Trân trọng,\n" +
                    "Đội ngũ %s",
                    fullName,
                    reason,
                    appName
            );

            message.setText(emailContent);
            mailSender.send(message);

            log.info("Ban email sent successfully to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send ban email to: {}", email, e);
        }
    }

    @Override
    @Scheduled(cron = "0 0 * * * *") // Chạy mỗi giờ
    @Transactional
    public void checkAndReactivateSuspendedAccounts() {
        log.info("Checking for accounts to reactivate...");

        LocalDateTime now = LocalDateTime.now();
        List<User> suspendedUsers = userRepository.findBySuspendedUntilBefore(now);

        for (User user : suspendedUsers) {
            if (!user.getIsBanned()) {
                log.info("Reactivating user {}", user.getId());

                user.setSuspendedUntil(null);
                user.setSuspensionReason(null);
                user.setStatus(Constants.StatusProfileEnum.ACTIVE);
                userRepository.save(user);

                // Gửi email thông báo mở lại tài khoản
                sendReactivationEmail(user.getEmail(), user.getFullName());

                // Gửi push notification (truyền cả recipientId và fcmToken)
                sendNotification(
                        user.getId().toString(),
                        user.getFcmToken(),
                        "🎉 Tài khoản đã được mở lại",
                        "Tài khoản của bạn đã hết thời gian đình chỉ và được kích hoạt lại."
                );

                log.info("User {} reactivated successfully", user.getId());
            }
        }

        log.info("Finished reactivating {} accounts", suspendedUsers.size());
    }

    private void sendReactivationEmail(String email, String fullName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject(String.format("[%s] Tài khoản đã được mở lại", appName));

            String emailContent = String.format(
                    "Kính gửi %s,\n\n" +
                    "Tài khoản của bạn đã hết thời gian đình chỉ và được kích hoạt lại.\n\n" +
                    "Bạn có thể đăng nhập và sử dụng các tính năng của hệ thống bình thường.\n\n" +
                    "Vui lòng tuân thủ quy định để tránh bị đình chỉ hoặc cấm tài khoản trong tương lai.\n\n" +
                    "Trân trọng,\n" +
                    "Đội ngũ %s",
                    fullName,
                    appName
            );

            message.setText(emailContent);
            mailSender.send(message);

            log.info("Reactivation email sent successfully to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send reactivation email to: {}", email, e);
        }
    }

    private void sendNotification(String fcmToken, String title, String message) {
        sendNotification(null, fcmToken, title, message);
    }

    private void sendNotification(String recipientId, String fcmToken, String title, String message) {
        try {
            // Nếu không có cả recipientId và fcmToken thì skip
            if ((recipientId == null || recipientId.isEmpty()) && (fcmToken == null || fcmToken.isEmpty())) {
                log.warn("Both recipientId and FCM token are null or empty, skipping notification");
                return;
            }

            // Nếu có recipientId thì ưu tiên dùng phương thức sendNotificationToUser
            if (recipientId != null && !recipientId.isEmpty()) {
                pushNotificationService.sendNotificationToUser(recipientId, title, message, "ACCOUNT", recipientId);
            } else {
                // Fallback về fcmToken nếu không có recipientId
                pushNotificationService.sendNotification(fcmToken, title, message);
            }
        } catch (Exception e) {
            log.error("Error sending push notification: {}", e.getMessage(), e);
        }
    }
}
