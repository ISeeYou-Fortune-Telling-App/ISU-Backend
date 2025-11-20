package com.iseeyou.fortunetelling.service.notification;

import java.util.Map;

public interface PushNotificationService {
    void sendNotification(String fcmToken, String title, String body);

    void sendNotification(String fcmToken, String title, String body, String imageUrl, Map<String, String> data);

    void sendNotificationToUser(String recipientId, String title, String body, String targetType, String targetId);

    void sendNotificationToMe(String title, String body, String imageUrl, Map<String, String> data, String targetType, String targetId);
}

