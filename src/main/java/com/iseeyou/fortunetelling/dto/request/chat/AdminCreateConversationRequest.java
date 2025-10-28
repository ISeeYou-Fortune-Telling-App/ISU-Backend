package com.iseeyou.fortunetelling.dto.request.chat;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminCreateConversationRequest {
    @NotNull(message = "Target user ID is required")
    private UUID targetUserId;  // ID của customer hoặc seer mà admin muốn chat

    private String initialMessage;  // Optional: Tin nhắn đầu tiên từ admin
}

