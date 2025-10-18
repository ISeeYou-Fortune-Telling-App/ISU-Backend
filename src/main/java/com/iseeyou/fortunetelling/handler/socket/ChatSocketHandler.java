package com.iseeyou.fortunetelling.handler.socket;

import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iseeyou.fortunetelling.dto.request.message.ChatMessageRequest;
import com.iseeyou.fortunetelling.dto.response.ChatMessageResponse;
import com.iseeyou.fortunetelling.entity.Conversation;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.exception.NotFoundException;
import com.iseeyou.fortunetelling.mapper.MessageMapper;
import com.iseeyou.fortunetelling.repository.converstation.ConversationRepository;
import com.iseeyou.fortunetelling.service.MessageSourceService;
import com.iseeyou.fortunetelling.service.message.MessageService;
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
public class ChatSocketHandler {
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

                // Verify user is participant
                Conversation conversation = conversationRepository.findById(convId)
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
        namespace.addEventListener("send_message", String.class, (client, messageJson, ackRequest) -> {
            try {
                // Parse JSON to ChatMessageRequest
                ObjectMapper objectMapper = new ObjectMapper();
                ChatMessageRequest request = objectMapper.readValue(messageJson, ChatMessageRequest.class);

                String userId = client.get("userId");
                User currentUser = userService.getUser();

                // Verify sender
                if (!currentUser.getId().toString().equals(userId)) {
                    ackRequest.sendAckData("error", messageSourceService.get("chat.unauthorized"));
                    return;
                }

                // Save message to DB
                ChatMessageResponse message = messageService.sendMessage(request.getConversationId(), request);

                // Broadcast to conversation room
                com.corundumstudio.socketio.BroadcastOperations roomOps = namespace.getRoomOperations(request.getConversationId().toString());
                if (roomOps != null) {
                    roomOps.sendEvent("receive_message", message);
                }

                log.info("Message sent in conversation {}: {}", request.getConversationId(), message.getId());
                ackRequest.sendAckData("success", message);
            } catch (Exception e) {
                log.error("Error sending message", e);
                ackRequest.sendAckData("error", e.getMessage());
            }
        });

        // Mark message as read
        namespace.addEventListener("mark_read", String.class, (client, messageId, ackRequest) -> {
            try {
                messageService.markMessageAsRead(UUID.fromString(messageId));
                log.info("Message {} marked as read", messageId);
                ackRequest.sendAckData("success");
            } catch (Exception e) {
                log.error("Error marking message as read", e);
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
