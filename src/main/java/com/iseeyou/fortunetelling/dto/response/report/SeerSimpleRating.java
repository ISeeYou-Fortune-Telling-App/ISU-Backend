package com.iseeyou.fortunetelling.dto.response.report;

import com.iseeyou.fortunetelling.util.Constants;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class SeerSimpleRating {
    private Integer totalRates;
    private BigDecimal avgRating;
    private String performanceTier;
    private Integer totalBookings;
    private Integer completedBookings;
}

