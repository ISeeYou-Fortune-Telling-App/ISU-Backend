package com.iseeyou.fortunetelling.entity;

import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.util.Constants;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notification")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@AttributeOverrides({
        @AttributeOverride(name = "id", column = @Column(name = "notification_id", nullable = false)),
})
public class Notification extends AbstractBaseEntity {
    @Column(name = "notification_type", nullable = false)
    @Builder.Default
    private Constants.NotificationTypeEnum notificationType = Constants.NotificationTypeEnum.ACCOUNT;

    @Column(name = "notification_title", nullable = false, columnDefinition = "TEXT")
    private String notificationTitle;

    @Column(name = "notification_body", nullable = false, columnDefinition = "TEXT")
    private String notificationBody;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;
}
