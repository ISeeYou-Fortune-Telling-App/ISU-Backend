package com.iseeyou.fortunetelling.dto.response;

import com.iseeyou.fortunetelling.util.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse extends AbstractBaseDataResponse {
    private UUID conversationId;
    private UUID senderId;
    private String senderName;
    private String senderAvatar;
    private String textContent;
    private String imageUrl;
    private String videoUrl;
    private Constants.MessageTypeEnum messageType;
    private Boolean isRead;
}
