package com.iseeyou.fortunetelling.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicePackageDetailResponse {
    // Thông tin Service Package
    private String packageId;
    private String packageTitle;
    private String packageContent;
    private String imageUrl;
    private Integer durationMinutes;
    private Double price;
    private String status;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Thông tin Seer
    private SeerInfo seer;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeerInfo {
        private String seerId;
        private String fullName;
        private String email;
        private String phone;
        private String avatarUrl;
        private String coverUrl;
        private String profileDescription;
        private Double avgRating;
        private Integer totalRates;
        private String paymentInfo;
    }
}
