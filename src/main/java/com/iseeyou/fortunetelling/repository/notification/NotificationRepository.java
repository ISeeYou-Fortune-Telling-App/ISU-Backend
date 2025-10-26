package com.iseeyou.fortunetelling.repository.notification;

import com.iseeyou.fortunetelling.entity.Notification;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.util.Constants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    @EntityGraph(attributePaths = {"recipient"})
    Page<Notification> findAllByRecipient(User recipient, Pageable pageable);

    @EntityGraph(attributePaths = {"recipient"})
    Page<Notification> findAllByRecipientAndNotificationType(
            User recipient,
            Constants.NotificationTypeEnum notificationType,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"recipient"})
    Page<Notification> findAllByRecipientAndIsRead(
            User recipient,
            Boolean isRead,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"recipient"})
    Page<Notification> findAllByRecipientAndNotificationTypeAndIsRead(
            User recipient,
            Constants.NotificationTypeEnum notificationType,
            Boolean isRead,
            Pageable pageable
    );

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipient = :recipient AND n.isRead = false")
    Long countUnreadByRecipient(@Param("recipient") User recipient);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipient = :recipient AND n.isRead = false")
    int markAllAsReadByRecipient(@Param("recipient") User recipient);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id IN :ids AND n.recipient = :recipient")
    int markAsReadByIds(@Param("ids") List<UUID> ids, @Param("recipient") User recipient);

    @Query("SELECT n FROM Notification n WHERE n.id IN :ids AND n.recipient = :recipient")
    List<Notification> findAllByIdsAndRecipient(@Param("ids") List<UUID> ids, @Param("recipient") User recipient);
}
