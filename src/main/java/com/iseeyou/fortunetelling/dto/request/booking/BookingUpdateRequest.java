package com.iseeyou.fortunetelling.dto.request.booking;

import com.iseeyou.fortunetelling.util.Constants;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class BookingUpdateRequest {
    private UUID bookingId;
    private LocalDateTime scheduledTime;
    private String additionalNote;
    private Constants.BookingStatusEnum status;
}
