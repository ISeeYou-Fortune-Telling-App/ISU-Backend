package com.iseeyou.fortunetelling.handler.socket;

import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import com.iseeyou.fortunetelling.service.socket.SocketIOService;
import com.iseeyou.fortunetelling.service.socket.impl.SocketIOServiceImpl;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationSocketHandler {
    private final SocketIOServer socketIOServer;
    private final SocketIOService socketIOService;

    @PostConstruct
    public void init() {
        SocketIONamespace namespace = socketIOServer.addNamespace("/notifications");

        // User connects to notification namespace
        namespace.addConnectListener(client -> {
            try {
                String userId = client.getHandshakeData().getSingleUrlParam("userId");

                if (userId == null || userId.isEmpty()) {
                    log.warn("User attempted to connect without userId");
                    client.disconnect();
                    return;
                }

                UUID userUuid = UUID.fromString(userId);
                client.set("userId", userId);

                // Register user in SocketIOService
                if (socketIOService instanceof SocketIOServiceImpl) {
                    ((SocketIOServiceImpl) socketIOService).registerUser(userUuid, client);
                }

                log.info("User {} connected to notifications namespace with socket id: {}", userId, client.getSessionId());

                // Send connection success event
                client.sendEvent("connect_success", Map.of(
                        "message", "Connected to notification service",
                        "userId", userId
                ));

            } catch (IllegalArgumentException e) {
                log.error("Invalid userId format", e);
                client.disconnect();
            } catch (Exception e) {
                log.error("Error during notification connection", e);
                client.disconnect();
            }
        });

        // User disconnects from notification namespace
        namespace.addDisconnectListener(client -> {
            try {
                String userId = client.get("userId");

                if (userId != null) {
                    UUID userUuid = UUID.fromString(userId);

                    // Unregister user from SocketIOService
                    if (socketIOService instanceof SocketIOServiceImpl) {
                        ((SocketIOServiceImpl) socketIOService).unregisterUser(userUuid);
                    }

                    log.info("User {} disconnected from notifications namespace", userId);
                }
            } catch (Exception e) {
                log.error("Error during notification disconnection", e);
            }
        });

        // Ping/Pong for connection health check
        namespace.addEventListener("ping", String.class, (client, data, ackRequest) -> {
            client.sendEvent("pong", Map.of("timestamp", System.currentTimeMillis()));
        });

        log.info("Notification Socket.IO namespace initialized: /notifications");
    }
}
