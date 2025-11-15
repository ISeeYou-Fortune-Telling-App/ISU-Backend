package com.iseeyou.fortunetelling.service.notification;

import java.util.Map;

public interface PushNotificationService {
    /**
     * Gửi push notification đến một FCM token
     *
     * @param fcmToken FCM token của người nhận
     * @param title Tiêu đề notification
     * @param body Nội dung notification
     */
    void sendNotification(String fcmToken, String title, String body);

    /**
     * Gửi push notification với metadata
     *
     * @param fcmToken FCM token của người nhận
     * @param title Tiêu đề notification
     * @param body Nội dung notification
     * @param imageUrl URL hình ảnh (optional)
     * @param data Dữ liệu bổ sung (optional)
     */
    void sendNotification(String fcmToken, String title, String body, String imageUrl, Map<String, String> data);

    /**
     * Gửi push notification đến một user bằng recipientId
     *
     * @param recipientId ID của người nhận
     * @param title Tiêu đề notification
     * @param body Nội dung notification
     * @param targetType Loại target (ACCOUNT, REPORT, etc.)
     * @param targetId ID của target
     */
    void sendNotificationToUser(String recipientId, String title, String body, String targetType, String targetId);
}

