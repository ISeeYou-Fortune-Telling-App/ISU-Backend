package com.iseeyou.fortunetelling.controller;

import com.iseeyou.fortunetelling.controller.base.AbstractBaseController;
import com.iseeyou.fortunetelling.dto.request.booking.BonusPaymentRequest;
import com.iseeyou.fortunetelling.dto.response.SingleResponse;
import com.iseeyou.fortunetelling.dto.response.booking.BookingPaymentResponse;
import com.iseeyou.fortunetelling.dto.response.error.ErrorResponse;
import com.iseeyou.fortunetelling.entity.booking.BookingPayment;
import com.iseeyou.fortunetelling.mapper.BookingMapper;
import com.iseeyou.fortunetelling.service.booking.BonusPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.iseeyou.fortunetelling.util.Constants.SECURITY_SCHEME_NAME;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
@Tag(name = "Admin Payment Management", description = "Admin APIs for payment management including bonus payments")
@Slf4j
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminPaymentController extends AbstractBaseController {

    private final BonusPaymentService bonusPaymentService;
    private final BookingMapper bookingMapper;

    @PostMapping("/bonus")
    @Operation(
            summary = "Create bonus payment for seer",
            description = "Admin endpoint to send bonus payment to a seer via PayPal. The seer must have a valid PayPal email configured.",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Bonus payment created and processed successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = BookingPaymentResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad request - Invalid input data",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Seer not found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Admin authentication required",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error - PayPal payout failed",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<SingleResponse<BookingPaymentResponse>> createBonusPayment(
            @Parameter(description = "Bonus payment request containing seerId, amount (VND), and reason", required = true)
            @Valid @RequestBody BonusPaymentRequest request
    ) {
        try {
            log.info("Admin creating bonus payment for seer {} with amount {} VND",
                    request.getSeerId(), request.getAmount());

            // TODO: Sau khi làm xong push notification, sử dụng reason để gửi thông báo tới seer
            BookingPayment bonusPayment = bonusPaymentService.createBonusPayment(
                    request.getSeerId(),
                    request.getAmount(),
                    request.getReason()
            );

            BookingPaymentResponse response = bookingMapper.toBookingPaymentResponse(bonusPayment);

            return responseFactory.successSingle(
                    response,
                    "Bonus payment created and sent to seer successfully"
            );

        } catch (IllegalArgumentException e) {
            log.error("Invalid bonus payment request: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to process bonus payment: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process bonus payment: " + e.getMessage(), e);
        }
    }
}

