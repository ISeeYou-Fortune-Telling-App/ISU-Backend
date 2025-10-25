package com.iseeyou.fortunetelling.dto.request.booking;

import com.iseeyou.fortunetelling.util.Constants;
import jakarta.validation.constraints.NotNull;
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
    
    @NotNull(message = "Payment method is required. Currently only PAYPAL is supported")
    private Constants.PaymentMethodEnum paymentMethod;
}
