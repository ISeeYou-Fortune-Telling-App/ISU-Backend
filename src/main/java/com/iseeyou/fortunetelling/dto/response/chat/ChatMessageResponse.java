package com.iseeyou.fortunetelling.dto.response.chat;

import com.iseeyou.fortunetelling.dto.response.AbstractBaseDataResponse;
import com.iseeyou.fortunetelling.util.Constants;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse extends AbstractBaseDataResponse {
    private UUID conversationId;

    // Customer info
    private UUID customerId;
    private String customerName;
    private String customerAvatar;

    // Seer info
    private UUID seerId;
    private String seerName;
    private String seerAvatar;

    // Message content
    private String textContent;
    private String imageUrl;
    private String videoUrl;
    private Constants.MessageTypeEnum messageType;
    private Constants.MessageStatusEnum status;
    private Constants.RoleEnum deletedBy;

    // Sender ID - frontend can compare with current user ID to determine if sentByMe
    private UUID senderId;
}
