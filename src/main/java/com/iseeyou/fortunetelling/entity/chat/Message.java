package com.iseeyou.fortunetelling.entity.chat;

import com.iseeyou.fortunetelling.entity.AbstractBaseEntity;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.util.Constants;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "message")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@AttributeOverrides({
        @AttributeOverride(name = "id", column = @Column(name = "message_id", nullable = false)),
})
public class Message extends AbstractBaseEntity {
    @Column(name = "text_content", columnDefinition = "text")
    private String textContent;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @Column(name = "message_type", length = 50)
    private String messageType;

    @Column(name = "status", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Constants.MessageStatusEnum status = Constants.MessageStatusEnum.UNREAD;

    @Column(name = "deleted_by", length = 20)
    @Enumerated(EnumType.STRING)
    private Constants.RoleEnum deletedBy;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    // Track which users have deleted this message on their side
    @ElementCollection
    @CollectionTable(
            name = "message_deleted_by",
            joinColumns = @JoinColumn(name = "message_id")
    )
    @Column(name = "user_id")
    private Set<UUID> deletedByUserIds = new HashSet<>();

    // Helper method to check if message is deleted for a specific user
    public boolean isDeletedForUser(UUID userId) {
        return deletedByUserIds.contains(userId);
    }

    @Column(name = "is_recalled", nullable = false)
    @Builder.Default
    private Boolean isRecalled = false;

    @Column(name = "recalled_at")
    private LocalDateTime recalledAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recalled_by")
    private User recalledBy;

    public boolean isVisibleForUser(UUID userId) {
        // Nếu recalled → không ai thấy
        if (isRecalled) {
            return false;
        }
        // Nếu user đã delete for me → không thấy
        return !deletedByUserIds.contains(userId);
    }
}
