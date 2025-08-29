package com.iseeyou.fortunetelling.controller;

import com.iseeyou.fortunetelling.controller.base.AbstractBaseController;
import com.iseeyou.fortunetelling.dto.request.booking.BookingCreateRequest;
import com.iseeyou.fortunetelling.dto.request.booking.BookingUpdateRequest;
import com.iseeyou.fortunetelling.dto.response.PageResponse;
import com.iseeyou.fortunetelling.dto.response.SingleResponse;
import com.iseeyou.fortunetelling.dto.response.booking.BookingPaymentResponse;
import com.iseeyou.fortunetelling.dto.response.booking.BookingRatingResponse;
import com.iseeyou.fortunetelling.dto.response.booking.BookingResponse;
import com.iseeyou.fortunetelling.dto.response.error.ErrorResponse;
import com.iseeyou.fortunetelling.entity.booking.Booking;
import com.iseeyou.fortunetelling.entity.booking.BookingPayment;
import com.iseeyou.fortunetelling.entity.booking.BookingReview;
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
    public ResponseEntity<SingleResponse<BookingResponse>> createBooking(
            @Parameter(description = "Service package ID", required = true)
            @PathVariable UUID servicePackageId,
            @Parameter(description = "Booking data to create", required = true)
            @RequestBody @Valid BookingCreateRequest request
    ) {
        Booking bookingToCreate = bookingMapper.mapTo(request, Booking.class);

        Booking createdBooking = bookingService.createBooking(
                bookingToCreate,
                servicePackageId,
                request.getPaymentMethod(),
                request.getSuccessUrl(),
                request.getCancelUrl()
        );

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
        Booking bookingToUpdate = bookingMapper.mapTo(request, Booking.class);
        bookingToUpdate.setId(id);

        Booking updatedBooking = bookingService.updateBooking(bookingToUpdate);
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

    @PostMapping("/{id}/refund")
    @Operation(
            summary = "Refund booking",
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

    @GetMapping("/{id}/reviews")
    @Operation(
            summary = "Get reviews for a booking",
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
    public ResponseEntity<PageResponse<BookingRatingResponse>> getBookingReviews(
            @Parameter(description = "Booking ID", required = true)
            @PathVariable UUID id,
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
        Page<BookingReview> reviews = bookingService.findAllReviewByBookingId(id, pageable);
        Page<BookingRatingResponse> response = bookingMapper.mapToPage(reviews, BookingRatingResponse.class);
        return responseFactory.successPage(response, "Booking reviews retrieved successfully");
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
    @Operation(summary = "Only for redirect URL from payment gateways")
    public ResponseEntity<SingleResponse<BookingPaymentResponse>> paymentSuccess(
            @RequestParam(value = "paymentId", required = false) String paymentId,
            @RequestParam(value = "PayerID", required = false) String payerId,
            @RequestParam(required = false) String vnp_BankCode,
            @RequestParam(required = false) String vnp_CardType,
            @RequestParam(required = false) String vnp_TransactionNo,
            @RequestParam(required = false) String vnp_ResponseCode,
            @RequestParam(required = false) String vnp_TxnRef
    ) {
        BookingPayment bookingPayment = null;
        if (paymentId != null && payerId != null) {
            bookingPayment = bookingService.executePayment(Constants.PaymentMethodEnum.PAYPAL, Map.of(
                    "paymentId", paymentId,
                    "PayerID", payerId
            ));
        } else {
            bookingPayment = bookingService.executePayment(Constants.PaymentMethodEnum.VNPAY, Map.of(
                    "vnp_BankCode", vnp_BankCode,
                    "vnp_CardType", vnp_CardType,
                    "vnp_TransactionNo", vnp_TransactionNo,
                    "vnp_ResponseCode", vnp_ResponseCode,
                    "vnp_TxnRef", vnp_TxnRef
            ));
        }
        BookingPaymentResponse response = bookingMapper.mapTo(bookingPayment, BookingPaymentResponse.class);
        return responseFactory.successSingle(response, "Payment executed successfully");
    }
    
    @GetMapping("/payment/cancel")
    @Operation(summary = "Only for redirect URL from payment gateways")
    public ResponseEntity<SingleResponse<String>> paymentCancel() {
        return responseFactory.successSingle("Payment cancelled", "Payment cancelled");
    }
}
