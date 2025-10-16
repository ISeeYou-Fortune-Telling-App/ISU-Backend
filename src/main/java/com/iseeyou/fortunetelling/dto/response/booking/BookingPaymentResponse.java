package com.iseeyou.fortunetelling.dto.response.booking;

import com.iseeyou.fortunetelling.dto.response.AbstractBaseDataResponse;
import com.iseeyou.fortunetelling.util.Constants;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class BookingPaymentResponse extends AbstractBaseDataResponse {
    private UUID bookingId;
    private Constants.PaymentStatusEnum paymentStatus;

    private BookingUserInfo customer;
    private BookingUserInfo seer;

    private String transactionId;
    private String packageTitle;
    private Constants.PaymentMethodEnum paymentMethod;
    private Double amount;
    private String failureReason;

    @Getter
    @Setter
    @NoArgsConstructor
    @SuperBuilder
    public static class BookingUserInfo {
        private String fullName;
        private String avatarUrl;
    }
}
