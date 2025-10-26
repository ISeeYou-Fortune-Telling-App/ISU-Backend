package com.iseeyou.fortunetelling.service.notification;

import com.iseeyou.fortunetelling.dto.request.notification.NotificationCreateRequest;
import com.iseeyou.fortunetelling.entity.Notification;
import com.iseeyou.fortunetelling.util.Constants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface NotificationService {
    Page<Notification> getMyNotifications(Pageable pageable);
    Page<Notification> getMyNotificationsByType(Constants.NotificationTypeEnum type, Pageable pageable);
    Page<Notification> getMyNotificationsByReadStatus(Boolean isRead, Pageable pageable);
    Page<Notification> getMyNotificationsByTypeAndReadStatus(Constants.NotificationTypeEnum type, Boolean isRead, Pageable pageable);

    Notification findById(UUID id);
    Long getUnreadCount();

    Notification createNotification(NotificationCreateRequest request);
    void markAsRead(UUID notificationId);
    void markMultipleAsRead(List<UUID> notificationIds);
    void markAllAsRead();
    String markAllAsReadWithUndo();
    void undoMarkAllAsRead(String undoToken);

    Page<Notification> getAllNotifications(Pageable pageable);
    Page<Notification> getAllNotificationsByRecipient(UUID recipientId, Pageable pageable);
}
