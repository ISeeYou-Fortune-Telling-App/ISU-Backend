package com.iseeyou.fortunetelling.dto.response.converstation;

import com.iseeyou.fortunetelling.dto.response.AbstractBaseDataResponse;
import com.iseeyou.fortunetelling.util.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionResponse extends AbstractBaseDataResponse {
    private UUID conversationId;
    private UUID bookingId;
    // PARTICIPANTS INFO
    private String seerName;
    private String seerAvatar;
    private String customerName;
    private String customerAvatar;
    // SESSION INFO
    private LocalDateTime sessionStartTime;
    private LocalDateTime sessionEndTime;
    private Integer sessionDurationMinutes;
    private Integer extendedMinutes;
    // Status
    private Constants.ConversationStatusEnum status;
    // Package info
    private String packageTitle;
    // LAST MESSAGE INFO
    private String lastMessageContent;
    private String lastMessageSenderName;
    private UUID lastMessageSenderId;
    private LocalDateTime lastMessageTime;
    // Conversation type
    private Constants.ConversationTypeEnum conversationType;
    // Message count
    private Integer totalMessages;
}
