package com.iseeyou.fortunetelling.listener;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import com.iseeyou.fortunetelling.dto.request.chat.session.CancelResponseRequest;
import com.iseeyou.fortunetelling.dto.request.chat.session.ChatMessageRequest;
import com.iseeyou.fortunetelling.dto.request.chat.session.PendingCancelRequest;
import com.iseeyou.fortunetelling.dto.request.chat.session.SendMultipleMessagesRequest;
import com.iseeyou.fortunetelling.dto.response.chat.session.ChatMessageResponse;
import com.iseeyou.fortunetelling.entity.chat.Conversation;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.exception.NotFoundException;
import com.iseeyou.fortunetelling.repository.chat.ConversationRepository;
import com.iseeyou.fortunetelling.service.MessageSourceService;
import com.iseeyou.fortunetelling.service.chat.ConversationService;
import com.iseeyou.fortunetelling.service.chat.MessageService;
import com.iseeyou.fortunetelling.service.user.UserService;
import com.iseeyou.fortunetelling.util.Constants;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatSocketListener {
    private final MessageService messageService;
    private final ConversationRepository conversationRepository;
    private final UserService userService;
    private final SocketIOServer socketIOServer;
    private final MessageSourceService messageSourceService;

    private final Map<UUID, PendingCancelRequest> pendingCancelRequests = new ConcurrentHashMap<>();
    private final ConversationService conversationService;

    @PostConstruct
    public void init() {
        SocketIONamespace namespace = socketIOServer.addNamespace("/chat");

        // User connects
        namespace.addConnectListener(client -> {
            String userId = client.getHandshakeData().getSingleUrlParam("userId");
            log.info("User connected: {} with socket id: {}", userId, client.getSessionId());

            client.set("userId", userId);
            User user = userService.findById(UUID.fromString(userId));
            client.set("user", user);
            client.sendEvent("connect_success", Map.of("message", messageSourceService.get("chat.connect.success")));
        });

        namespace.addEventListener("admin_join_all_conversations", String.class, (client, userId, ackRequest) -> {
            try {
                // Get all conversations (both ADMIN_CHAT and BOOKING_SESSION)
                // So admin can monitor all conversations and see messages from users
                List<Conversation> allConversations = conversationRepository.findAll();

                log.info("Admin is joining {} total conversations", allConversations.size());

                // Join each conversation room
                int joinedCount = 0;
                for (Conversation conversation : allConversations) {
                    String convId = conversation.getId().toString();
                    client.joinRoom(convId);
                    joinedCount++;
                }

                log.info("User {} successfully joined {} conversation rooms", userId, joinedCount);

                // Send success response with conversation IDs
                List<String> conversationIds = allConversations.stream()
                        .map(c -> c.getId().toString())
                        .toList();

                ackRequest.sendAckData(Map.of(
                        "status", "success",
                        "message", "Admin joined all conversations",
                        "conversationIds", conversationIds,
                        "totalJoined", joinedCount
                ));

            } catch (Exception e) {
                log.error("Error in admin_join_all_conversations", e);
                ackRequest.sendAckData(Map.of(
                        "status", "error",
                        "message", e.getMessage()
                ));
            }
        });

        // User joins conversation room
        namespace.addEventListener("join_conversation", String.class, (client, conversationId, ackRequest) -> {
            try {
                UUID convId = UUID.fromString(conversationId);
                String userId = client.get("userId");

                // Verify user is participant - use JOIN FETCH to avoid lazy loading
                Conversation conversation = conversationRepository.findByIdWithDetails(convId)
                        .orElseThrow(() -> new NotFoundException("Conversation not found"));

                boolean isParticipant = false;

                // Check if admin chat
                if (conversation.getType().equals(Constants.ConversationTypeEnum.ADMIN_CHAT)) {
                    // For admin chat: verify user is admin or target user
                    isParticipant = (conversation.getAdmin() != null && conversation.getAdmin().getId().toString().equals(userId)) ||
                                  (conversation.getTargetUser() != null && conversation.getTargetUser().getId().toString().equals(userId));
                } else if (conversation.getBooking() != null) {
                    // For booking session: verify user is customer or seer
                    isParticipant = conversation.getBooking().getCustomer().getId().toString().equals(userId) ||
                                  conversation.getBooking().getServicePackage().getSeer().getId().toString().equals(userId);
                }

                if (!isParticipant) {
                    ackRequest.sendAckData("error", messageSourceService.get("chat.unauthorized"));
                    return;
                }

                // Join room
                client.joinRoom(conversationId);
                log.info("User {} joined conversation {}", userId, conversationId);

                // Track join time only for booking sessions
                if (conversation.getBooking() != null) {
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
                User currentUser = client.get("user");

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

        // Send message to multiple conversations
        namespace.addEventListener("send_messages", SendMultipleMessagesRequest.class, (client, request, ackRequest) -> {
            try {
                String userId = client.get("userId");
                log.info("User {} sending message to {} conversations", userId, request.getConversationIds().size());

                // Get user by ID
                User currentUser = client.get("user");

                if (currentUser == null) {
                    log.error("User not found: {}", userId);
                    ackRequest.sendAckData("error", "User not found");
                    return;
                }

                // Create message request for each conversation
                ChatMessageRequest messageRequest = new ChatMessageRequest();
                messageRequest.setTextContent(request.getTextContent());

                // Send messages to all conversations
                boolean success = messageService.sendMessages(request.getConversationIds(), messageRequest, currentUser);

                if (success) {
                    log.info("Messages sent successfully to {} conversations", request.getConversationIds().size());

                    // Broadcast to each conversation room
                    for (UUID conversationId : request.getConversationIds()) {
                        // Get the saved message for this conversation
                        ChatMessageRequest singleRequest = new ChatMessageRequest();
                        singleRequest.setConversationId(conversationId);
                        singleRequest.setTextContent(request.getTextContent());

                        ChatMessageResponse message = messageService.sendMessage(conversationId, singleRequest, currentUser);

                        com.corundumstudio.socketio.BroadcastOperations roomOps = namespace.getRoomOperations(conversationId.toString());
                        if (roomOps != null) {
                            roomOps.sendEvent("receive_message", message);
                            log.info("Message broadcasted to room: {}", conversationId);
                        }
                    }

                    ackRequest.sendAckData("success");
                } else {
                    ackRequest.sendAckData("error", "Failed to send messages");
                }

            } catch (Exception e) {
                log.error("Error sending messages to multiple conversations", e);
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

        namespace.addEventListener("cancel_session_manually", String.class, (client, conversationId, ackRequest) -> {
            try {
                Conversation conversation = conversationRepository.findById(UUID.fromString(conversationId)).orElse(null);

                if (conversation == null) {
                    ackRequest.sendAckData("error", "Conversation not found");
                    return;
                }

                User currentUser = client.get("user");
                String userId = client.get("userId");
                User otherUser;

                if (currentUser.getRole() == Constants.RoleEnum.SEER) {
                    // Other user is customer
                    otherUser = conversation.getBooking().getCustomer();
                } else {
                    // Other user is seer
                    otherUser = conversation.getBooking().getServicePackage().getSeer();
                }

                PendingCancelRequest pendingCancelRequest = PendingCancelRequest.builder()
                        .conversationId(UUID.fromString(conversationId))
                        .requesterId(currentUser.getId())
                        .respondentId(otherUser.getId())
                        .requesterRole(currentUser.getRole())
                        .requestedAt(LocalDateTime.now())
                        .build();

                pendingCancelRequests.put(pendingCancelRequest.getConversationId(), pendingCancelRequest);

                // Send cancel confirmation request to the other user
                sendToUser(namespace, otherUser.getId().toString(), Map.of(
                    "conversationId", conversationId,
                    "requesterId", userId,
                    "requesterName", currentUser.getFullName(),
                    "timestamp", LocalDateTime.now().toString()
                ));

                log.info("Cancel request sent to user {} for confirmation", otherUser.getId());
                ackRequest.sendAckData("success", "Cancel request sent for confirmation");
            } catch (Exception e) {
                log.error("Error in cancel_session_manually", e);
                ackRequest.sendAckData("error", e.getMessage());
            }
        });

        // B responds to cancel request
        namespace.addEventListener("respond_cancel_request", CancelResponseRequest.class, (client, response, ackRequest) -> {
            try {
                UUID conversationId = response.getConversationId();
                boolean confirmed = response.isConfirmed();
                String respondentId = client.get("userId");

                log.info("User {} responded to cancel request for conversation {}: {}",
                    respondentId, conversationId, confirmed);

                // Check if pending cancel request exists
                PendingCancelRequest pendingRequest = pendingCancelRequests.get(conversationId);
                if (pendingRequest == null) {
                    ackRequest.sendAckData("error", "No pending cancel request found");
                    return;
                }

                // Check if user B is authorized to respond to this request
                if (!pendingRequest.getRespondentId().equals(UUID.fromString(respondentId))) {
                    ackRequest.sendAckData("error", "You are not authorized to respond to this request");
                    return;
                }

                if (confirmed) {
                    // B confirmed cancellation - proceed with cancellation
                    conversationService.cancelSession(conversationId, pendingRequest.getRequesterRole());

                    // Notify both A and B
                    String cancelMessage = messageSourceService.get("chat.session.cancelled");

                    // Send to A (requester)
                    sendToUser(namespace, pendingRequest.getRequesterId().toString(), "cancel_result", Map.of(
                        "status", "success",
                        "message", cancelMessage,
                        "conversationId", conversationId.toString(),
                        "cancelledBy", pendingRequest.getRequesterId().toString(),
                        "confirmedBy", respondentId,
                        "timestamp", LocalDateTime.now().toString()
                    ));

                    // Send to B (respondent)
                    sendToUser(namespace, respondentId, "cancel_result", Map.of(
                        "status", "success",
                        "message", cancelMessage,
                        "conversationId", conversationId.toString(),
                        "cancelledBy", pendingRequest.getRequesterId().toString(),
                        "confirmedBy", respondentId,
                        "timestamp", LocalDateTime.now().toString()
                    ));

                    // Broadcast to conversation room
                    com.corundumstudio.socketio.BroadcastOperations roomOps =
                        namespace.getRoomOperations(conversationId.toString());
                    if (roomOps != null) {
                        roomOps.sendEvent("session_cancelled", Map.of(
                            "conversationId", conversationId.toString(),
                            "cancelledBy", pendingRequest.getRequesterId().toString(),
                            "message", cancelMessage,
                            "timestamp", LocalDateTime.now().toString()
                        ));
                    }

                    log.info("Session {} cancelled successfully by user {}", conversationId, pendingRequest.getRequesterId());

                } else {
                    // B declined cancellation
                    String declineMessage = messageSourceService.get("chat.session.cancel_declined");

                    // Notify A (requester)
                    sendToUser(namespace, pendingRequest.getRequesterId().toString(), "cancel_result", Map.of(
                        "status", "declined",
                        "message", declineMessage,
                        "conversationId", conversationId.toString(),
                        "declinedBy", respondentId,
                        "timestamp", LocalDateTime.now().toString()
                    ));

                    log.info("Session cancellation declined by user {} for conversation {}", respondentId, conversationId);
                }

                // Remove pending request from map
                pendingCancelRequests.remove(conversationId);
                ackRequest.sendAckData("success");

            } catch (Exception e) {
                log.error("Error processing cancel response", e);
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
        log.info("✅ Socket.IO server started successfully on port {}", socketIOServer.getConfiguration().getPort());
        log.info("Socket.IO listening on: {}:{}",
                socketIOServer.getConfiguration().getHostname(),
                socketIOServer.getConfiguration().getPort());
    }

    /**
     * Send an event to a specific user by their userId with custom event name
     */
    private void sendToUser(SocketIONamespace namespace, String userId, String eventName, Object data) {
        for (SocketIOClient client : namespace.getAllClients()) {
            String clientUserId = client.get("userId");
            if (userId.equals(clientUserId)) {
                client.sendEvent(eventName, data);
                log.info("Event '{}' sent to user {}", eventName, userId);
                break;
            }
        }
    }

    /**
     * Send request_cancel_confirmation event to a specific user
     */
    private void sendToUser(SocketIONamespace namespace, String userId, Object data) {
        sendToUser(namespace, userId, "request_cancel_confirmation", data);
    }

    @PreDestroy
    public void destroy() {
        log.info("Stopping Socket.IO server...");
        socketIOServer.stop();
        log.info("✅ Socket.IO server stopped");
    }


}
