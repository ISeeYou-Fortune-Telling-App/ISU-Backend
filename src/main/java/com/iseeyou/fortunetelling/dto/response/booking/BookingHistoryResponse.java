package com.iseeyou.fortunetelling.dto.response.booking;

import com.iseeyou.fortunetelling.dto.response.AbstractBaseDataResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
public class BookingHistoryResponse extends AbstractBaseDataResponse {
    private LocalDateTime scheduledTime;
    private String status;
    private String additionalNote;
    private Double amount;
    private String customerName;
    private String servicePackageName;
    private String paymentStatus;
}
