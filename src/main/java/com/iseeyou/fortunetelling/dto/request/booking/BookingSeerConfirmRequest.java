// ...new file...
package com.iseeyou.fortunetelling.dto.request.booking;

import com.iseeyou.fortunetelling.util.Constants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingSeerConfirmRequest {
    @NotNull(message = "Status is required")
    @Schema(example = "CONFIRMED OR CANCELED")
    private Constants.BookingStatusEnum status;
}

