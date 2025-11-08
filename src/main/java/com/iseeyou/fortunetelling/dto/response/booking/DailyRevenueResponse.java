package com.iseeyou.fortunetelling.dto.response.booking;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyRevenueResponse {
    @JsonProperty("date")
    private LocalDate date;

    @JsonProperty("total_revenue")
    private Double totalRevenue;

    @JsonProperty("tax_percentage")
    private Double taxPercentage;
}
