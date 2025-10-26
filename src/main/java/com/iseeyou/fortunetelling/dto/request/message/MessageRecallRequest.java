package com.iseeyou.fortunetelling.dto.request.message;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageRecallRequest {
    @NotEmpty(message = "Message IDs are required")
    @Size(max = 10, message = "Cannot recall more than 10 messages at once")
    private List<UUID> messageIds;
}
