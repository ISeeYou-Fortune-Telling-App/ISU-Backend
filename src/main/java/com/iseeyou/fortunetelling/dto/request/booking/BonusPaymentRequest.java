package com.iseeyou.fortunetelling.dto.request.booking;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BonusPaymentRequest {
    @NotNull(message = "Seer ID không được để trống")
    private UUID seerId;

    @NotNull(message = "Số tiền thưởng không được để trống")
    @Positive(message = "Số tiền thưởng phải lớn hơn 0")
    private Double amount;

    private String reason;
}

