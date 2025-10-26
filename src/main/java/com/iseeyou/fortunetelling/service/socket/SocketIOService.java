package com.iseeyou.fortunetelling.service.socket;

import com.iseeyou.fortunetelling.dto.response.notification.NotificationResponse;

import java.util.List;
import java.util.UUID;

public interface SocketIOService {
    // Send notification to a specific user via SocketIO
    void sendNotificationToUser(UUID userId, NotificationResponse notificationResponse);
    // Send notification to multiple users
    void sendNotificationToUsers(List<UUID> userIds, NotificationResponse notificationResponse);
    // Broadcast notification to all connected users (Admin only)
    void broadcastNotification(NotificationResponse notificationResponse);
    boolean isUserOnline(UUID userId);
    // Get count of online users
    int getOnlineUsersCount();
}
