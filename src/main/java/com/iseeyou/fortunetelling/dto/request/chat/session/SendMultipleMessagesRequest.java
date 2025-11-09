package com.iseeyou.fortunetelling.dto.request.chat.session;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendMultipleMessagesRequest {
    @NotNull(message = "Conversation IDs are required")
    @NotEmpty(message = "At least one conversation ID is required")
    private List<UUID> conversationIds;

    private String textContent;
}

