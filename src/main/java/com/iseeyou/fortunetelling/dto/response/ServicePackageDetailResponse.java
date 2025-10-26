package com.iseeyou.fortunetelling.dto.response;

import com.iseeyou.fortunetelling.util.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
    private Constants.ServiceCategoryEnum category; // thêm trường category
    private String status;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Review statistics
    private Double avgRating;
    private Long totalReviews;
    private List<ReviewInfo> reviews;

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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewInfo {
        private UUID bookingId;
        private BigDecimal rating;
        private String comment;
        private LocalDateTime reviewedAt;
        private CustomerInfo customer;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerInfo {
        private UUID customerId;
        private String customerName;
        private String customerAvatar;
    }
}
