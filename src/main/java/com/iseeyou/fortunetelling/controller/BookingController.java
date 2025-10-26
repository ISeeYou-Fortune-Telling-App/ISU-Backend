package com.iseeyou.fortunetelling.controller;

import com.iseeyou.fortunetelling.controller.base.AbstractBaseController;
import com.iseeyou.fortunetelling.dto.request.booking.BookingCreateRequest;
import com.iseeyou.fortunetelling.dto.request.booking.BookingReviewRequest;
import com.iseeyou.fortunetelling.dto.request.booking.BookingUpdateRequest;
import com.iseeyou.fortunetelling.dto.response.PageResponse;
import com.iseeyou.fortunetelling.dto.response.SingleResponse;
import com.iseeyou.fortunetelling.dto.response.booking.BookingPaymentResponse;
import com.iseeyou.fortunetelling.dto.response.booking.BookingRatingResponse;
import com.iseeyou.fortunetelling.dto.response.booking.BookingResponse;
import com.iseeyou.fortunetelling.dto.response.booking.BookingReviewResponse;
import com.iseeyou.fortunetelling.dto.response.error.ErrorResponse;
import com.iseeyou.fortunetelling.entity.booking.Booking;
import com.iseeyou.fortunetelling.entity.booking.BookingPayment;
import com.iseeyou.fortunetelling.mapper.BookingMapper;
import com.iseeyou.fortunetelling.service.booking.BookingService;
import com.iseeyou.fortunetelling.util.Constants;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

import static com.iseeyou.fortunetelling.util.Constants.SECURITY_SCHEME_NAME;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bookings")
@Tag(name = "003. Booking", description = "Booking API")
@Slf4j
public class BookingController extends AbstractBaseController {
    private final BookingService bookingService;
    private final BookingMapper bookingMapper;

    @GetMapping("/my-bookings")
    @Operation(
            summary = "Get my bookings with pagination, leave the status empty to get all bookings",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful operation",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = PageResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'SEER', 'ADMIN')")
    public ResponseEntity<PageResponse<BookingResponse>> getMyBookings(
            @Parameter(description = "Page number (1-based)")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "15") int limit,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "desc") String sortType,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Filter by booking status")
            @RequestParam(required = false) Constants.BookingStatusEnum status
    ) {
        Pageable pageable = createPageable(page, limit, sortType, sortBy);
        Page<Booking> bookings;

        if (status != null) {
            bookings = bookingService.getBookingsByMeAndStatus(status, pageable);
        } else {
            bookings = bookingService.getBookingsByMe(pageable);
        }

        Page<BookingResponse> response = bookingMapper.mapToPage(bookings, BookingResponse.class);
        return responseFactory.successPage(response, "Bookings retrieved successfully");
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get booking by ID",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful operation",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SingleResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Booking not found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<SingleResponse<BookingResponse>> getBookingById(
            @Parameter(description = "Booking ID", required = true)
            @PathVariable UUID id
    ) {
        Booking booking = bookingService.findById(id);
        BookingResponse response = bookingMapper.mapTo(booking, BookingResponse.class);
        return responseFactory.successSingle(response, "Booking retrieved successfully");
    }

    @PostMapping("/{servicePackageId}")
    @Operation(
            summary = "Create a new booking for a service package",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Booking created successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SingleResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad request",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<SingleResponse<BookingResponse>> createBooking(
            @Parameter(description = "Service package ID", required = true)
            @PathVariable UUID servicePackageId,
            @Parameter(description = "Booking data to create", required = true)
            @RequestBody @Valid BookingCreateRequest request
    ) {
        Booking createdBooking = bookingService.createBooking(request, servicePackageId);
        BookingResponse response = bookingMapper.mapTo(createdBooking, BookingResponse.class);
        return responseFactory.successSingle(response, "Booking created successfully");
    }

    @PatchMapping("/{id}")
    @Operation(
            summary = "Update booking",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Booking updated successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SingleResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad request",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Booking not found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<SingleResponse<BookingResponse>> updateBooking(
            @Parameter(description = "Booking ID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Booking data to update", required = true)
            @RequestBody @Valid BookingUpdateRequest request
    ) {
        Booking updatedBooking = bookingService.updateBooking(id, request);
        BookingResponse response = bookingMapper.mapTo(updatedBooking, BookingResponse.class);
        return responseFactory.successSingle(response, "Booking updated successfully");
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete booking",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Booking deleted successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SingleResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Booking not found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<SingleResponse<String>> deleteBooking(
            @Parameter(description = "Booking ID", required = true)
            @PathVariable UUID id
    ) {
        bookingService.deleteBooking(id);
        return responseFactory.successSingle("Booking deleted successfully", "Booking deleted successfully");
    }

    @PostMapping("/{id}/cancel")
    @Operation(
            summary = "Cancel booking (Customer only)",
            description = "Allows customer to cancel their booking. Booking must be cancelled at least 2 hours before scheduled time. " +
                         "If payment was completed, refund will be processed automatically. Only PENDING or CONFIRMED bookings can be cancelled.",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Booking cancelled successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SingleResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Booking not found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Booking cannot be cancelled - already cancelled, completed, failed, or less than 2 hours before scheduled time",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - only booking customer can cancel",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<SingleResponse<BookingResponse>> cancelBooking(
            @Parameter(description = "Booking ID", required = true)
            @PathVariable UUID id
    ) {
        Booking cancelledBooking = bookingService.cancelBooking(id);
        BookingResponse response = bookingMapper.mapTo(cancelledBooking, BookingResponse.class);
        return responseFactory.successSingle(response, "Booking cancelled and refund processed successfully");
    }

    @PostMapping("/{id}/refund")
    @Operation(
            summary = "Refund booking (Admin only)",
            description = "Administrative endpoint to manually refund a booking. Use /cancel endpoint for customer-initiated cancellations.",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Booking refunded successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SingleResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Booking not found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Booking cannot be refunded",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<SingleResponse<BookingResponse>> refundBooking(
            @Parameter(description = "Booking ID", required = true)
            @PathVariable UUID id
    ) {
        Booking refundedBooking = bookingService.refundBooking(id);
        BookingResponse response = bookingMapper.mapTo(refundedBooking, BookingResponse.class);
        return responseFactory.successSingle(response, "Booking refunded successfully");
    }

    @PostMapping("/{bookingId}/review")
    @Operation(
            summary = "Submit a review for a completed booking",
            description = "Customers can submit a review (rating 1.0-5.0 and optional comment) for completed bookings. Each booking can only be reviewed once.",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Review submitted successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SingleResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad request - booking not completed or already reviewed",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Booking not found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - only booking customer can review",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<SingleResponse<BookingReviewResponse>> submitReview(
            @Parameter(description = "Booking ID", required = true)
            @PathVariable UUID bookingId,
            @Parameter(description = "Review data (rating and optional comment)", required = true)
            @Valid @RequestBody BookingReviewRequest reviewRequest
    ) {
        BookingReviewResponse response = bookingService.submitReview(bookingId, reviewRequest);
        return responseFactory.successSingle(response, "Review submitted successfully");
    }

    @GetMapping("/reviews/service-package/{packageId}")
    @Operation(
            summary = "Get all reviews for a service package",
            description = "Get paginated list of all reviews for a specific service package",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Reviews retrieved successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = PageResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Service package not found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<PageResponse<BookingReviewResponse>> getReviewsByServicePackage(
            @Parameter(description = "Service Package ID", required = true)
            @PathVariable UUID packageId,
            @Parameter(description = "Page number (1-based)")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "15") int limit,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "desc") String sortType,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "reviewedAt") String sortBy
    ) {
        Pageable pageable = createPageable(page, limit, sortType, sortBy);
        Page<BookingReviewResponse> response = bookingService.getReviewsByServicePackage(packageId, pageable);
        return responseFactory.successPage(response, "Reviews retrieved successfully");
    }

    @GetMapping("/payments")
    @Operation(
            summary = "Get booking payments with filters",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful operation",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = PageResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<PageResponse<BookingPaymentResponse>> getBookingPayments(
            @Parameter(description = "Page number (1-based)")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "15") int limit,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "desc") String sortType,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Filter by payment method")
            @RequestParam(required = false) Constants.PaymentMethodEnum paymentMethod,
            @Parameter(description = "Filter by payment status")
            @RequestParam(required = false) Constants.PaymentStatusEnum paymentStatus
    ) {
        Pageable pageable = createPageable(page, limit, sortType, sortBy);
        Page<BookingPayment> payments;

        if (paymentMethod != null) {
            payments = bookingService.findAllByPaymentMethod(paymentMethod, pageable);
        } else if (paymentStatus != null) {
            payments = bookingService.findAllByStatus(paymentStatus, pageable);
        } else {
            // If no filters, you might need to add a findAllPayments method to the service
            // For now, let's use findAllByStatus with null or implement a default behavior
            throw new UnsupportedOperationException("Please provide either paymentMethod or paymentStatus filter");
        }

        Page<BookingPaymentResponse> response = bookingMapper.mapToPage(payments, BookingPaymentResponse.class);
        return responseFactory.successPage(response, "Booking payments retrieved successfully");
    }

    @GetMapping("/payments/{paymentId}")
    @Operation(
            summary = "Get booking payment by ID",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful operation",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SingleResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Payment not found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<SingleResponse<BookingPaymentResponse>> getBookingPaymentById(
            @Parameter(description = "Payment ID", required = true)
            @PathVariable UUID paymentId
    ) {
        BookingPayment payment = bookingService.findPaymentById(paymentId);
        BookingPaymentResponse response = bookingMapper.mapTo(payment, BookingPaymentResponse.class);
        return responseFactory.successSingle(response, "Booking payment retrieved successfully");
    }

    @GetMapping("/payment/success")
    @Operation(summary = "Only for redirect URL from payment gateways (Currently only PayPal)")
    public ResponseEntity<SingleResponse<BookingPaymentResponse>> paymentSuccess(
            @RequestParam(value = "paymentId", required = true) String paymentId,
            @RequestParam(value = "PayerID", required = true) String payerId
            // VNPay parameters temporarily disabled
            // @RequestParam(required = false) String vnp_BankCode,
            // @RequestParam(required = false) String vnp_CardType,
            // @RequestParam(required = false) String vnp_TransactionNo,
            // @RequestParam(required = false) String vnp_ResponseCode,
            // @RequestParam(required = false) String vnp_TxnRef
    ) {
        // Only PayPal is supported temporarily
        BookingPayment bookingPayment = bookingService.executePayment(
                Constants.PaymentMethodEnum.PAYPAL, 
                Map.of(
                        "paymentId", paymentId,
                        "PayerID", payerId
                )
        );
        
        // VNPay disabled
        // if (paymentId != null && payerId != null) {
        //     bookingPayment = bookingService.executePayment(Constants.PaymentMethodEnum.PAYPAL, Map.of(
        //             "paymentId", paymentId,
        //             "PayerID", payerId
        //     ));
        // } else {
        //     bookingPayment = bookingService.executePayment(Constants.PaymentMethodEnum.VNPAY, Map.of(
        //             "vnp_BankCode", vnp_BankCode,
        //             "vnp_CardType", vnp_CardType,
        //             "vnp_TransactionNo", vnp_TransactionNo,
        //             "vnp_ResponseCode", vnp_ResponseCode,
        //             "vnp_TxnRef", vnp_TxnRef
        //     ));
        // }
        
        BookingPaymentResponse response = bookingMapper.mapTo(bookingPayment, BookingPaymentResponse.class);
        return responseFactory.successSingle(response, "Payment executed successfully");
    }

    @GetMapping("/payment/cancel")
    @Operation(summary = "Only for redirect URL from payment gateways")
    public ResponseEntity<SingleResponse<String>> paymentCancel() {
        return responseFactory.successSingle("Payment cancelled", "Payment cancelled");
    }

    @GetMapping("/payments/invalid")
    @Operation(
            summary = "Get payments with invalid transaction IDs (Admin/Debug)",
            description = "Returns payments that have invalid or missing transaction IDs. Useful for debugging payment issues.",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful operation",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = PageResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<PageResponse<BookingPaymentResponse>> getInvalidPayments(
            @Parameter(description = "Page number (1-based)")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "15") int limit,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "desc") String sortType,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "createdAt") String sortBy
    ) {
        Pageable pageable = createPageable(page, limit, sortType, sortBy);
        Page<BookingPayment> invalidPayments = bookingService.findPaymentsWithInvalidTransactionIds(pageable);
        Page<BookingPaymentResponse> response = bookingMapper.mapToPage(invalidPayments, BookingPaymentResponse.class);
        return responseFactory.successPage(response, "Invalid payments retrieved successfully");
    }
}
