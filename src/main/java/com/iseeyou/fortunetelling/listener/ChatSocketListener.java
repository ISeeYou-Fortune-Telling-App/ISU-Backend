package com.iseeyou.fortunetelling.listener;

import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iseeyou.fortunetelling.dto.request.chat.ChatMessageRequest;
import com.iseeyou.fortunetelling.dto.response.chat.ChatMessageResponse;
import com.iseeyou.fortunetelling.entity.chat.Conversation;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.exception.NotFoundException;
import com.iseeyou.fortunetelling.repository.chat.ConversationRepository;
import com.iseeyou.fortunetelling.service.MessageSourceService;
import com.iseeyou.fortunetelling.service.chat.MessageService;
import com.iseeyou.fortunetelling.service.user.UserService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatSocketListener {
    private final MessageService messageService;
    private final ConversationRepository conversationRepository;
    private final UserService userService;
    private final SocketIOServer socketIOServer;
    private final MessageSourceService messageSourceService;

    @PostConstruct
    public void init() {
        SocketIONamespace namespace = socketIOServer.addNamespace("/chat");

        // User connects
        namespace.addConnectListener(client -> {
            String userId = client.getHandshakeData().getSingleUrlParam("userId");
            log.info("User connected: {} with socket id: {}", userId, client.getSessionId());

            client.set("userId", userId);
            client.sendEvent("connect_success", Map.of("message", messageSourceService.get("chat.connect.success")));
        });

        // User joins conversation room
        namespace.addEventListener("join_conversation", String.class, (client, conversationId, ackRequest) -> {
            try {
                UUID convId = UUID.fromString(conversationId);
                String userId = client.get("userId");

                // Verify user is participant - use JOIN FETCH to avoid lazy loading
                Conversation conversation = conversationRepository.findByIdWithDetails(convId)
                        .orElseThrow(() -> new NotFoundException("Conversation not found"));

                boolean isParticipant = conversation.getBooking().getCustomer().getId().toString().equals(userId) ||
                        conversation.getBooking().getServicePackage().getSeer().getId().toString().equals(userId);

                if (!isParticipant) {
                    ackRequest.sendAckData("error", messageSourceService.get("chat.unauthorized"));
                    return;
                }

                // Join room
                client.joinRoom(conversationId);
                log.info("User {} joined conversation {}", userId, conversationId);

                // Track customer join time
                boolean isCustomer = conversation.getBooking().getCustomer().getId().toString().equals(userId);
                boolean isSeer = conversation.getBooking().getServicePackage().getSeer().getId().toString().equals(userId);

                if (isCustomer && conversation.getCustomerJoinedAt() == null) {
                    conversation.setCustomerJoinedAt(LocalDateTime.now());
                    conversationRepository.save(conversation);
                    log.info("Customer joined for conversation: {}", conversationId);
                } else if (isSeer && conversation.getSeerJoinedAt() == null) {
                    conversation.setSeerJoinedAt(LocalDateTime.now());
                    conversationRepository.save(conversation);
                    log.info("Seer joined for conversation: {}", conversationId);
                }

                // Notify others in room
                com.corundumstudio.socketio.BroadcastOperations roomOps = namespace.getRoomOperations(conversationId);
                if (roomOps != null) {
                    roomOps.sendEvent("user_joined",
                            Map.of(
                                    "userId", userId,
                                    "message", messageSourceService.get("chat.user.joined"),
                                    "timestamp", LocalDateTime.now().toString()
                            ));
                }

                ackRequest.sendAckData("success");
            } catch (Exception e) {
                log.error("Error joining conversation", e);
                ackRequest.sendAckData("error", e.getMessage());
            }
        });

        // User leaves conversation
        namespace.addEventListener("leave_conversation", String.class, (client, conversationId, ackRequest) -> {
            try {
                client.leaveRoom(conversationId);
                String userId = client.get("userId");
                log.info("User {} left conversation {}", userId, conversationId);

                com.corundumstudio.socketio.BroadcastOperations roomOps = namespace.getRoomOperations(conversationId);
                if (roomOps != null) {
                    roomOps.sendEvent("user_left", Map.of("userId", userId));
                }

                ackRequest.sendAckData("success");
            } catch (Exception e) {
                log.error("Error leaving conversation", e);
                ackRequest.sendAckData("error", e.getMessage());
            }
        });

        // Send message event
        namespace.addEventListener("send_message", ChatMessageRequest.class, (client, request, ackRequest) -> {
            try {
                String userId = client.get("userId");
                log.info("User {} sending message to conversation {}", userId, request.getConversationId());

                // Get user by ID (Socket.IO doesn't have SecurityContext)
                User currentUser = userService.findById(UUID.fromString(userId));

                if (currentUser == null) {
                    log.error("User not found: {}", userId);
                    ackRequest.sendAckData("error", "User not found");
                    return;
                }

                // Save message to DB with sender
                ChatMessageResponse message = messageService.sendMessage(request.getConversationId(), request, currentUser);

                log.info("Message saved successfully: {}", message.getId());

                // Broadcast to ALL participants in conversation room (including sender)
                com.corundumstudio.socketio.BroadcastOperations roomOps = namespace.getRoomOperations(request.getConversationId().toString());
                if (roomOps != null) {
                    roomOps.sendEvent("receive_message", message);
                    log.info("Message broadcasted to room: {}", request.getConversationId());
                }

                // Send success acknowledgment
                ackRequest.sendAckData("success");

            } catch (Exception e) {
                log.error("Error sending message", e);
                ackRequest.sendAckData("error", e.getMessage());
            }
        });

        // Mark messages as read (all unread messages in conversation, except those sent by me)
        namespace.addEventListener("mark_read", String.class, (client, conversationId, ackRequest) -> {
            try {
                messageService.markMessagesAsRead(UUID.fromString(conversationId));
                log.info("Marked unread messages as read in conversation {}", conversationId);
                ackRequest.sendAckData("success");
            } catch (Exception e) {
                log.error("Error marking messages as read", e);
                ackRequest.sendAckData("error", e.getMessage());
            }
        });

        // User disconnects
        namespace.addDisconnectListener(client -> {
            String userId = client.get("userId");
            log.info("User disconnected: {}", userId);
        });

        // Start server
        socketIOServer.start();
        log.info("Socket.IO server started successfully");
    }

    @PreDestroy
    public void destroy() {
        if (socketIOServer != null) {
            socketIOServer.stop();
            log.info("Socket.IO server stopped");
        }
    }
}
