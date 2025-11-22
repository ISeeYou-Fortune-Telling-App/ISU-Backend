package com.iseeyou.fortunetelling.dto.request.chat.session;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelResponseRequest {
    private UUID conversationId;
    private boolean confirmed;
}