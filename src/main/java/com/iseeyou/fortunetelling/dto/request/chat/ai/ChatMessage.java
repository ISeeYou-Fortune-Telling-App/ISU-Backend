package com.iseeyou.fortunetelling.dto.request.chat.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String role; // "user" or "assistant"
    private String content;
    private Long timestamp;
}
