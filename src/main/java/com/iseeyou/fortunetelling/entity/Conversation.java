package com.iseeyou.fortunetelling.entity;

import com.iseeyou.fortunetelling.entity.booking.Booking;
import com.iseeyou.fortunetelling.util.Constants;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "conversation")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@AttributeOverrides({
        @AttributeOverride(name = "id", column = @Column(name = "conversation_id", nullable = false)),
})
public class Conversation extends AbstractBaseEntity {
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "type", length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    private Constants.ConversationTypeEnum type;

    @Column(name = "session_start_time")
    private LocalDateTime sessionStartTime;

    @Column(name = "session_end_time")
    private LocalDateTime sessionEndTime;

    @Column(name = "session_duration_minutes")
    private Integer sessionDurationMinutes;

    @Column(name = "status", length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Constants.ConversationStatusEnum status = Constants.ConversationStatusEnum.ACTIVE;

    @OneToMany(mappedBy = "conversation", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Message> messages;

    @Column(name = "customer_joined_at")
    private LocalDateTime customerJoinedAt;  // Track khi customer join

    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;  // Lý do hủy

    @Column(name = "warning_notification_sent")
    @Builder.Default
    private Boolean warningNotificationSent = false;  // Đã gửi warning chưa

    @Column(name = "extended_minutes")
    @Builder.Default
    private Integer extendedMinutes = 0;
}