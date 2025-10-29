package com.iseeyou.fortunetelling.dto.response.servicepackage;

import com.iseeyou.fortunetelling.util.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@SuperBuilder
public class ServicePackageResponse {
    private String id;
    private SeerInfo seer;
    private String packageTitle;
    private String packageContent;
    private String imageUrl;
    private Integer durationMinutes;
    private Double price;
    private List<CategoryInfo> categories; // Đổi từ single category sang list categories
    private Constants.PackageStatusEnum status;
    private String rejectionReason;
    private Long likeCount;
    private Long dislikeCount;
    private List<UserInteractionInfo> userInteractions; // Array of users who interacted
    private Double avgRating; // Average rating from booking reviews
    private Long totalReviews; // Total number of reviews
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @SuperBuilder
    public static class SeerInfo {
        private UUID id;
        private String fullName;
        private String avatarUrl;
        private Double avgRating;
        private Integer totalRates;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryInfo {
        private UUID id;
        private String name;
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInteractionInfo {
        private UUID userId;
        private String name;
        private String avatar;
        private String typeInteract; // LIKE or DISLIKE
    }
}
