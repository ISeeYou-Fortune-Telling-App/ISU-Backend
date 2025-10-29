package com.iseeyou.fortunetelling.dto.request.booking;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Request to submit a review for a completed booking")
public class BookingReviewRequest {
    
    @NotNull(message = "Rating is required")
    @DecimalMin(value = "1.0", message = "Rating must be at least 1.0")
    @DecimalMax(value = "5.0", message = "Rating must not exceed 5.0")
    @Digits(integer = 1, fraction = 1, message = "Rating must have at most 1 decimal place")
    @Schema(description = "Rating from 1.0 to 5.0", example = "4.5", required = true)
    private BigDecimal rating;
    
    @Size(max = 1000, message = "Comment must not exceed 1000 characters")
    @Schema(description = "Optional review comment", example = "Great service!", maxLength = 1000)
    private String comment;
}

