package com.iseeyou.fortunetelling.dto.response.booking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingReviewResponse {
    private UUID bookingId;
    private BigDecimal rating;
    private String comment;
    private LocalDateTime reviewedAt;
    
    // Customer info
    private CustomerInfo customer;
    
    // Service package info
    private ServicePackageInfo servicePackage;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CustomerInfo {
        private UUID customerId;
        private String customerName;
        private String customerAvatar;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ServicePackageInfo {
        private UUID packageId;
        private String packageTitle;
    }
}

