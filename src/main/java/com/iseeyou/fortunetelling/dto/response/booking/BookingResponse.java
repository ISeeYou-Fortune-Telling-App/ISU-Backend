package com.iseeyou.fortunetelling.dto.response.booking;

import com.iseeyou.fortunetelling.dto.response.AbstractBaseDataResponse;
import com.iseeyou.fortunetelling.util.Constants;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class BookingResponse extends AbstractBaseDataResponse {
    private Constants.BookingStatusEnum status;
    private BookingSeerInfo seer;
    private BookingCustomerInfo customer;
    private ServicePackageInfo servicePackage;
    private LocalDateTime scheduledTime;
    private String additionalNote;
    private BookingPaymentInfo[] bookingPaymentInfos;
    private String redirectUrl;

    @Getter
    @Setter
    @NoArgsConstructor
    @SuperBuilder
    public static class BookingSeerInfo {
        private String fullName;
        private String avatarUrl;
        private Double avgRating;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @SuperBuilder
    public static class BookingCustomerInfo {
        private String fullName;
        private String avatarUrl;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @SuperBuilder
    public static class ServicePackageInfo {
        private String packageTitle;
        private String packageContent;
        private Double price;
        private Integer durationMinutes;
        private List<String> categories; // This is the name of the knowledge category
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @SuperBuilder
    public static class BookingPaymentInfo {
        private Double amount;
        private Constants.PaymentMethodEnum paymentMethod;
        private Constants.PaymentStatusEnum paymentStatus;
        private LocalDateTime paymentTime;
        private String approvalUrl;
        private String failureReason;
    }
}
