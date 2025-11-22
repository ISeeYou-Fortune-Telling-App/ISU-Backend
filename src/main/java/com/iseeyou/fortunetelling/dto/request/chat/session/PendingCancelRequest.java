package com.iseeyou.fortunetelling.dto.request.chat.session;

import com.iseeyou.fortunetelling.util.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingCancelRequest {
    private UUID conversationId;
    private UUID requesterId;
    private UUID respondentId;
    private Constants.RoleEnum requesterRole;
    private LocalDateTime requestedAt;
}
