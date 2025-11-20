package com.iseeyou.fortunetelling.service.notification.impl;

import com.iseeyou.fortunetelling.service.notification.PushNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PushNotificationServiceImpl implements PushNotificationService {

    private final RestTemplate restTemplate;

    @Value("${notification.service.uri:http://host.docker.internal:8085}")
    private String notificationServiceUri;

    @Override
    public void sendNotification(String fcmToken, String title, String body) {
        sendNotification(fcmToken, title, body, null, null);
    }

    @Override
    public void sendNotification(String fcmToken, String title, String body, String imageUrl, Map<String, String> data) {
        sendNotificationWithRecipient(null, fcmToken, title, body, imageUrl, data, "ACCOUNT", "SYSTEM");
    }

    @Override
    public void sendNotificationToUser(String recipientId, String title, String body, String targetType, String targetId) {
        sendNotificationWithRecipient(recipientId, null, title, body, null, null, targetType, targetId);
    }

    /**
     * Gửi notification với cả recipientId và fcmToken
     * Push Notification service sẽ ưu tiên recipientId nếu có, không thì dùng fcmToken
     */
    private void sendNotificationWithRecipient(String recipientId, String fcmToken, String title, String body,
                                               String imageUrl, Map<String, String> data,
                                               String targetType, String targetId) {
        try {
            // Nếu không có cả recipientId và fcmToken thì skip
            if ((recipientId == null || recipientId.isEmpty()) && (fcmToken == null || fcmToken.isEmpty())) {
                log.warn("Both recipientId and FCM token are null or empty, skipping notification");
                return;
            }

            String url = notificationServiceUri + "/notification";

            Map<String, Object> request = new HashMap<>();

            // Truyền cả recipientId và fcmToken
             if (recipientId != null && !recipientId.isEmpty()) {
                request.put("recipientId", recipientId);
            } else {
                request.put("recipientId", "SYSTEM");
            }

            if (fcmToken != null && !fcmToken.isEmpty()) {
                request.put("fcmToken", fcmToken);
            } else {
                request.put("fcmToken", "");
            }

            request.put("notificationTitle", title);
            request.put("notificationBody", body);
            request.put("targetType", targetType != null ? targetType : "ACCOUNT");
            request.put("targetId", targetId != null ? targetId : "SYSTEM");

            if (imageUrl != null && !imageUrl.isEmpty()) {
                request.put("imageUrl", imageUrl);
            }

            if (data != null && !data.isEmpty()) {
                request.put("metaData", data);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                if (recipientId != null && !recipientId.isEmpty()) {
                    log.info("Push notification sent successfully to recipientId: {}", recipientId);
                } else if (fcmToken != null && !fcmToken.isEmpty()) {
                    log.info("Push notification sent successfully to FCM token: {}",
                            fcmToken.substring(0, Math.min(20, fcmToken.length())) + "...");
                }
            } else {
                log.error("Failed to send push notification. Status: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error sending push notification: {}", e.getMessage(), e);
        }
    }
}
