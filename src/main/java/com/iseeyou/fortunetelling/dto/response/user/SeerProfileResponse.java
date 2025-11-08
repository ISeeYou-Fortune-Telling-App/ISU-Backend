package com.iseeyou.fortunetelling.dto.response.user;

import com.iseeyou.fortunetelling.util.Constants;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class SeerProfileResponse {
    private Double avgRating;
    private Integer totalRates;
    private String paymentInfo;
    private String seerTier;

    // Thống kê booking
    private Long totalBookings;
    private Long completedBookings;
    private Double totalRevenue;
}