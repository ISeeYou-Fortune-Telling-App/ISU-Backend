package com.iseeyou.fortunetelling.dto.response.booking;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingPaymentStatsResponse {
    private Double totalRevenue;
    private Long successfulTransactions;
    private Long refundedTransactions;
    private Double totalRefundedAmount;
}
