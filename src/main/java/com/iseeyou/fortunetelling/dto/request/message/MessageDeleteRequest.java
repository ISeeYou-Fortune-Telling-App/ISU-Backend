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
public class MessageDeleteRequest {
    @NotEmpty(message = "Message IDs are required")
    @Size(max = 50, message = "Cannot delete more than 50 messages at once")
    private List<UUID> messageIds;
}
