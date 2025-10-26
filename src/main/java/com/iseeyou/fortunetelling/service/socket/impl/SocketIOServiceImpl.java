package com.iseeyou.fortunetelling.service.socket.impl;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.iseeyou.fortunetelling.dto.response.notification.NotificationResponse;
import com.iseeyou.fortunetelling.service.socket.SocketIOService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class SocketIOServiceImpl implements SocketIOService {

    private final SocketIOServer socketIOServer;

    // Map userId to socketClient for targeted push
    private final Map<UUID, SocketIOClient> userSocketMap = new ConcurrentHashMap<>();

    private static final String NOTIFICATION_EVENT = "new-notification";

    @Override
    public void sendNotificationToUser(UUID userId, NotificationResponse notificationResponse) {
        SocketIOClient client = userSocketMap.get(userId);

        if (client != null && client.isChannelOpen()) {
            client.sendEvent(NOTIFICATION_EVENT, notificationResponse);
            log.info("Sent notification to user {} via SocketIO", userId);
        } else {
            log.debug("User {} is not online, skipping SocketIO push", userId);
        }
    }

    @Override
    public void sendNotificationToUsers(List<UUID> userIds, NotificationResponse notificationResponse) {
        userIds.forEach(userId -> sendNotificationToUser(userId, notificationResponse));
    }

    @Override
    public void broadcastNotification(NotificationResponse notificationResponse) {
        socketIOServer.getBroadcastOperations().sendEvent(NOTIFICATION_EVENT, notificationResponse);
        log.info("Broadcasted notification to all connected users");
    }

    @Override
    public boolean isUserOnline(UUID userId) {
        SocketIOClient client = userSocketMap.get(userId);
        return client != null && client.isChannelOpen();
    }

    @Override
    public int getOnlineUsersCount() {
        return userSocketMap.size();
    }

    // Public methods for NotificationSocketHandler
    public void registerUser(UUID userId, SocketIOClient client) {
        userSocketMap.put(userId, client);
        log.info("User {} registered for notifications", userId);
    }

    public void unregisterUser(UUID userId) {
        userSocketMap.remove(userId);
        log.info("User {} unregistered from notifications", userId);
    }
}
