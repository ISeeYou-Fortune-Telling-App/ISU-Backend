package com.iseeyou.fortunetelling.dto.Internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeletedMessageInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID userId;
    private UUID conversationId;
    private List<UUID> messageIds;
    private LocalDateTime deletedAt;
}
