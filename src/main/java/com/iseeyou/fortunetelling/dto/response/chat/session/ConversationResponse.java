package com.iseeyou.fortunetelling.dto.response.chat.session;

import com.iseeyou.fortunetelling.dto.response.AbstractBaseDataResponse;
import com.iseeyou.fortunetelling.util.Constants;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse extends AbstractBaseDataResponse {
    private UUID conversationId;

    // Seer info
    private UUID seerId;
    private String seerName;
    private String seerAvatarUrl;

    // Customer info
    private UUID customerId;
    private String customerName;
    private String customerAvatarUrl;

    // Session info
    private LocalDateTime sessionStartTime;
    private LocalDateTime sessionEndTime;
    private Integer sessionDurationMinutes;

    // Unread counts
    private Integer seerUnreadCount;
    private Integer customerUnreadCount;
    private Integer adminUnreadCount;

    // Last message
    private String lastMessageContent;
    private LocalDateTime lastMessageTime;

    // Status
    private Constants.ConversationStatusEnum status;
    private String sessionCanceledBy;
    private LocalDateTime sessionCanceledTime;
}
