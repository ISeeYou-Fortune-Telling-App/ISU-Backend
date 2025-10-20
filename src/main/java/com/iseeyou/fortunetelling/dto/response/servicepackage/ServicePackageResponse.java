package com.iseeyou.fortunetelling.dto.response.servicepackage;

import com.iseeyou.fortunetelling.util.Constants;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
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
    private Constants.ServiceCategoryEnum category;
    private Constants.PackageStatusEnum status;
    private String rejectionReason;
    private Long likeCount;
    private Long dislikeCount;
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
}
