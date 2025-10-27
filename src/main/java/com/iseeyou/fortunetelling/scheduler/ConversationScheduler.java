package com.iseeyou.fortunetelling.scheduler;

import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import com.iseeyou.fortunetelling.entity.chat.Conversation;
import com.iseeyou.fortunetelling.repository.chat.ConversationRepository;
import com.iseeyou.fortunetelling.service.MessageSourceService;
import com.iseeyou.fortunetelling.service.chat.ConversationService;
import com.iseeyou.fortunetelling.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConversationScheduler {
    private final ConversationRepository conversationRepository;
    private final ConversationService conversationService;
    private final SocketIOServer socketIOServer;
    private final MessageSourceService messageSourceService;

    @Scheduled(fixedRate = 60000)  // 1 mins  ms
    public void checkSessions() {
        log.debug("Running scheduled session checks...");

        checkAndCancelLateSessions();
        checkAndNotifyEndingSessions();
        checkAndAutoEndExpiredSessions();
    }

    private void checkAndCancelLateSessions() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(10);

        List<Conversation> lateSessions = conversationRepository.findLateSessions(
                Constants.ConversationStatusEnum.ACTIVE,
                cutoffTime
        );

        if (lateSessions.isEmpty()) {
            return;
        }

        log.info("Found {} late sessions to cancel", lateSessions.size());

        for (Conversation conversation : lateSessions) {
            try {
                // Determine who is late
                boolean customerLate = conversation.getCustomerJoinedAt() == null;
                boolean seerLate = conversation.getSeerJoinedAt() == null;

                String reason;
                if (customerLate && seerLate) {
                    reason = "Both customer and seer late >10 minutes";
                } else if (customerLate) {
                    reason = "Customer late >10 minutes";
                } else {
                    reason = "Seer late >10 minutes";
                }

                // Cancel conversation & booking
                conversationService.cancelLateSession(conversation.getId());

                // Notify qua Socket.IO
                SocketIONamespace namespace = socketIOServer.getNamespace("/chat");
                namespace.getRoomOperations(conversation.getId().toString())
                        .sendEvent("session_canceled", Map.of(
                                "conversationId", conversation.getId().toString(),
                                "reason", reason,
                                "canceledBy", customerLate && seerLate ? "BOTH" : (customerLate ? "CUSTOMER" : "SEER"),
                                "message", messageSourceService.get("chat.session.canceled.late"),
                                "timestamp", LocalDateTime.now().toString()
                        ));

                log.info("Canceled late session: conversationId={}, reason={}", conversation.getId(), reason);
            } catch (Exception e) {
                log.error("Error canceling late session: conversationId={}",
                        conversation.getId(), e);
            }
        }
    }

    private void checkAndNotifyEndingSessions() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime warningTime = now.plusMinutes(10);

        List<Conversation> endingSessions = conversationRepository.findSessionsNeedingWarning(
                Constants.ConversationStatusEnum.ACTIVE,
                now,
                warningTime
        );

        if (endingSessions.isEmpty()) {
            return;
        }

        log.info("Found {} sessions needing warning notification", endingSessions.size());

        for (Conversation conversation : endingSessions) {
            try {
                // Mark warning sent
                conversationService.sendWarningNotification(conversation.getId());

                // Calculate remaining minutes
                long remainingMinutes = java.time.Duration.between(
                        now,
                        conversation.getSessionEndTime()
                ).toMinutes();

                // Notify qua Socket.IO
                SocketIONamespace namespace = socketIOServer.getNamespace("/chat");
                namespace.getRoomOperations(conversation.getId().toString())
                        .sendEvent("session_ending_soon", Map.of(
                                "conversationId", conversation.getId().toString(),
                                "remainingMinutes", remainingMinutes,
                                "message", String.format(messageSourceService.get("chat.session.ending.soon"), remainingMinutes),
                                "canExtend", true,  // Frontend có thể show extend button
                                "timestamp", LocalDateTime.now().toString()
                        ));

                log.info("Sent warning notification: conversationId={}, remainingMinutes={}",
                        conversation.getId(), remainingMinutes);
            } catch (Exception e) {
                log.error("Error sending warning notification: conversationId={}",
                        conversation.getId(), e);
            }
        }
    }

    private void checkAndAutoEndExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();

        List<Conversation> expiredSessions = conversationRepository.findExpiredSessions(
                Constants.ConversationStatusEnum.ACTIVE,
                now
        );

        if (expiredSessions.isEmpty()) {
            return;
        }

        log.info("Found {} expired sessions to end", expiredSessions.size());

        for (Conversation conversation : expiredSessions) {
            try {
                // End conversation & complete booking
                conversationService.autoEndSession(conversation.getId());

                // Notify qua Socket.IO
                SocketIONamespace namespace = socketIOServer.getNamespace("/chat");
                namespace.getRoomOperations(conversation.getId().toString())
                        .sendEvent("session_ended", Map.of(
                                "conversationId", conversation.getId().toString(),
                                "reason", "Session time expired",
                                "message", messageSourceService.get("chat.session.ended"),
                                "timestamp", LocalDateTime.now().toString()
                        ));

                log.info("Auto-ended expired session: conversationId={}", conversation.getId());
            } catch (Exception e) {
                log.error("Error auto-ending session: conversationId={}",
                        conversation.getId(), e);
            }
        }
    }
}
