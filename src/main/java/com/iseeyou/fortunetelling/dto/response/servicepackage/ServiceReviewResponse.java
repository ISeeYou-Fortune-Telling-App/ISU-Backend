package com.iseeyou.fortunetelling.dto.response.servicepackage;

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
public class ServiceReviewResponse {
    private UUID reviewId;
    private String comment;
    private String createdAt;
    private String updatedAt;

    private UserInfo user;
    private UUID parentReviewId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfo {
        private UUID userId;
        private String fullName;
        private String avatarUrl;
    }
}

