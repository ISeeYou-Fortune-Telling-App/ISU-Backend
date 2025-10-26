package com.iseeyou.fortunetelling.service.notification.impl;

import com.iseeyou.fortunetelling.dto.request.notification.NotificationCreateRequest;
import com.iseeyou.fortunetelling.dto.response.notification.NotificationResponse;
import com.iseeyou.fortunetelling.entity.Notification;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.exception.NotFoundException;
import com.iseeyou.fortunetelling.exception.UnauthorizedException;
import com.iseeyou.fortunetelling.mapper.NotificationMapper;
import com.iseeyou.fortunetelling.repository.notification.NotificationRepository;
import com.iseeyou.fortunetelling.repository.user.UserRepository;
import com.iseeyou.fortunetelling.service.notification.NotificationService;
import com.iseeyou.fortunetelling.service.socket.SocketIOService;
import com.iseeyou.fortunetelling.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SocketIOService socketIOService;

    private static final String UNDO_PREFIX = "notification:undo:";
    private static final long UNDO_TIMEOUT_SECONDS = 10;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<Notification> getMyNotifications(Pageable pageable) {
        User currentUser = getCurrentUser();
        return notificationRepository.findAllByRecipient(currentUser, pageable);
    }

    @Override
    public Page<Notification> getMyNotificationsByType(Constants.NotificationTypeEnum type, Pageable pageable) {
        User currentUser = getCurrentUser();
        return notificationRepository.findAllByRecipientAndNotificationType(currentUser, type, pageable);
    }

    @Override
    public Page<Notification> getMyNotificationsByReadStatus(Boolean isRead, Pageable pageable) {
        User currentUser = getCurrentUser();
        return notificationRepository.findAllByRecipientAndIsRead(currentUser, isRead, pageable);
    }

    @Override
    public Page<Notification> getMyNotificationsByTypeAndReadStatus(Constants.NotificationTypeEnum type, Boolean isRead, Pageable pageable) {
        User currentUser = getCurrentUser();
        return notificationRepository.findAllByRecipientAndNotificationTypeAndIsRead(currentUser, type, isRead, pageable);
    }

    @Override
    public Notification findById(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Notification not found with id: " + id));

        User currentUser = getCurrentUser();
        if (!notification.getRecipient().getId().equals(currentUser.getId()) &&
                !currentUser.getRole().equals(Constants.RoleEnum.ADMIN)) {
            throw new UnauthorizedException("You are not authorized to view this notification");
        }

        return notification;
    }

    @Override
    public Long getUnreadCount() {
        User currentUser = getCurrentUser();
        return notificationRepository.countUnreadByRecipient(currentUser);
    }

    @Override
    public Notification createNotification(NotificationCreateRequest request) {
        User recipient = userRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new NotFoundException("User not found with id: " + request.getRecipientId()));

        Notification notification = Notification.builder()
                .notificationType(request.getNotificationType())
                .notificationTitle(request.getNotificationTitle())
                .notificationBody(request.getNotificationBody())
                .recipient(recipient)
                .isRead(false)
                .build();

        Notification savedNotification = notificationRepository.save(notification);
        log.info("Created notification {} for user {}", savedNotification.getId(), recipient.getId());

        try {
            if (socketIOService.isUserOnline(recipient.getId())) {
                NotificationResponse response = notificationMapper.mapTo(savedNotification, NotificationResponse.class);
                socketIOService.sendNotificationToUser(recipient.getId(), response);
                log.info("Pushed notification {} to user {} via SocketIO", savedNotification.getId(), recipient.getId());
            } else {
                log.debug("User {} is offline, notification will be delivered when they connect", recipient.getId());
            }
        } catch (Exception e) {
            log.error("Failed to push notification via SocketIO", e);
        }

        return savedNotification;
    }

    @Override
    public void markAsRead(UUID notificationId) {
        Notification notification = findById(notificationId);

        if (!notification.getIsRead()) {
            notification.setIsRead(true);
            notificationRepository.save(notification);
            log.info("Marked notification {} as read", notificationId);
        }
    }

    @Override
    public void markMultipleAsRead(List<UUID> notificationIds) {
        User currentUser = getCurrentUser();
        int updatedCount = notificationRepository.markAsReadByIds(notificationIds, currentUser);
        log.info("Marked {} notifications as read for user {}", updatedCount, currentUser.getId());
    }

    @Override
    public void markAllAsRead() {
        User currentUser = getCurrentUser();
        int updatedCount = notificationRepository.markAllAsReadByRecipient(currentUser);
        log.info("Marked all {} notifications as read for user {}", updatedCount, currentUser.getId());
    }

    @Override
    public String markAllAsReadWithUndo() {
        User currentUser = getCurrentUser();

        // Lấy tất cả notifications chưa đọc trước khi mark as read
        List<Notification> unreadNotifications = notificationRepository
                .findAllByRecipientAndIsRead(currentUser, false, Pageable.unpaged())
                .getContent();

        if (unreadNotifications.isEmpty()) {
            return null;
        }

        // Lưu danh sách IDs vào Redis với timeout
        String undoToken = UUID.randomUUID().toString();
        List<UUID> notificationIds = unreadNotifications.stream()
                .map(Notification::getId)
                .collect(Collectors.toList());

        redisTemplate.opsForValue().set(
                UNDO_PREFIX + undoToken,
                notificationIds,
                UNDO_TIMEOUT_SECONDS,
                TimeUnit.SECONDS
        );

        // Mark all as read
        markAllAsRead();

        log.info("Marked all notifications as read with undo token {} for user {}",
                undoToken, currentUser.getId());

        return undoToken;
    }

    @Override
    public void undoMarkAllAsRead(String undoToken) {
        User currentUser = getCurrentUser();
        String redisKey = UNDO_PREFIX + undoToken;

        @SuppressWarnings("unchecked")
        List<UUID> notificationIds = (List<UUID>) redisTemplate.opsForValue().get(redisKey);

        if (notificationIds == null || notificationIds.isEmpty()) {
            throw new NotFoundException("Undo token expired or invalid");
        }

        // Lấy notifications và mark as unread
        List<Notification> notifications = notificationRepository
                .findAllByIdsAndRecipient(notificationIds, currentUser);

        notifications.forEach(n -> n.setIsRead(false));
        notificationRepository.saveAll(notifications);

        // Xóa undo token
        redisTemplate.delete(redisKey);

        log.info("Undone mark all as read for user {}, restored {} notifications",
                currentUser.getId(), notifications.size());
    }

    @Override
    public Page<Notification> getAllNotifications(Pageable pageable) {
        User currentUser = getCurrentUser();
        if (!currentUser.getRole().equals(Constants.RoleEnum.ADMIN)) {
            throw new UnauthorizedException("Only admin can view all notifications");
        }
        return notificationRepository.findAll(pageable);
    }

    @Override
    public Page<Notification> getAllNotificationsByRecipient(UUID recipientId, Pageable pageable) {
        User currentUser = getCurrentUser();
        if (!currentUser.getRole().equals(Constants.RoleEnum.ADMIN)) {
            throw new UnauthorizedException("Only admin can view notifications of other users");
        }

        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + recipientId));

        return notificationRepository.findAllByRecipient(recipient, pageable);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));
    }
}
