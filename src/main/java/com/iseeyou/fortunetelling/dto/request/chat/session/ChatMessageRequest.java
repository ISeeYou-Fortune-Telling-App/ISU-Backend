package com.iseeyou.fortunetelling.dto.request.chat.session;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRequest {
    @NotNull(message = "Conversation ID is required")
    private UUID conversationId;

    private String textContent;
    private String imagePath;
    private String videoPath;
}
