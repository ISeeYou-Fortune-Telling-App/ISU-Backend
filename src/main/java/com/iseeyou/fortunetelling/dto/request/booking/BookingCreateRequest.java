package com.iseeyou.fortunetelling.dto.request.booking;

import com.iseeyou.fortunetelling.util.Constants;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class BookingCreateRequest {
    private LocalDateTime scheduledTime;
    private String additionalNote;
    private Constants.PaymentMethodEnum paymentMethod;
    private String successUrl;
    private String cancelUrl;
}
