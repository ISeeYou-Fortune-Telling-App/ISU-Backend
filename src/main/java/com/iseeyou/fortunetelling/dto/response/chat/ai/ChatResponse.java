package com.iseeyou.fortunetelling.dto.response.chat.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private Boolean sentByUser;
    private String textContent;
    private String analysisType;
    private String imageUrl;
    private Double processingTime;
    private LocalDateTime createdAt;
}
