package com.iseeyou.fortunetelling.dto.request.chat;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRequest {
    @NotNull(message = "Conversation ID is required")
    private UUID conversationId;

    private String textContent;

    private String imageUrl;
    private String videoUrl;
}
