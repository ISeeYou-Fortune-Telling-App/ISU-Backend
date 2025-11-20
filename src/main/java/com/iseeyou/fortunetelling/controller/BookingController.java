package com.iseeyou.fortunetelling.controller;

import com.iseeyou.fortunetelling.controller.base.AbstractBaseController;
import com.iseeyou.fortunetelling.dto.request.booking.BookingCreateRequest;
import com.iseeyou.fortunetelling.dto.request.booking.BookingReviewRequest;
import com.iseeyou.fortunetelling.dto.request.booking.BookingUpdateRequest;
import com.iseeyou.fortunetelling.dto.request.booking.BookingSeerConfirmRequest;
import com.iseeyou.fortunetelling.dto.response.PageResponse;
import com.iseeyou.fortunetelling.dto.response.SingleResponse;
import com.iseeyou.fortunetelling.dto.response.booking.BookingPageResponse;
import com.iseeyou.fortunetelling.dto.response.booking.BookingPaymentResponse;
import com.iseeyou.fortunetelling.dto.response.booking.BookingResponse;
import com.iseeyou.fortunetelling.dto.response.booking.BookingReviewResponse;
import com.iseeyou.fortunetelling.dto.response.booking.CreateBookingResponse;
import com.iseeyou.fortunetelling.dto.response.booking.DailyRevenueResponse;
import com.iseeyou.fortunetelling.service.booking.impl.BookingServiceImpl;
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
            @Parameter(description = "Filter by booking status (optional)")
            @RequestParam(required = false) Constants.BookingStatusEnum status,
            @Parameter(description = "Filter by year (optional)")
            @RequestParam(required = false) Integer year,
            @Parameter(description = "Filter by month (1-12, optional)")
            @RequestParam(required = false) Integer month,
            @Parameter(description = "Filter by day (1-31, optional)")
            @RequestParam(required = false) Integer day
    ) {
        // If no date filters provided, keep original behavior (delegated to service for DB pagination)
        if (year == null && month == null && day == null) {
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

        // When any date filter is provided, fetch a bounded result set then filter in-memory.
        // Assumption: per-user bookings are reasonably sized. MAX_FETCH prevents unbounded queries.
        final int MAX_FETCH = 10000;
        Pageable fetchPageable = createPageable(1, MAX_FETCH, sortType, sortBy);
        Page<Booking> fetchedPage;

        if (status != null) {
            fetchedPage = bookingService.getBookingsByMeAndStatus(status, fetchPageable);
        } else {
            fetchedPage = bookingService.getBookingsByMe(fetchPageable);
        }

        java.util.List<Booking> fetched = fetchedPage.getContent();

        // Determine filter mode: full date > month+year > year only
        java.time.LocalDate targetDate = null;
        if (year != null && month != null && day != null) {
            try {
                targetDate = java.time.LocalDate.of(year, month, day);
            } catch (Exception ex) {
                throw new IllegalArgumentException("Invalid date parameters: " + ex.getMessage());
            }
        }

        java.util.List<Booking> filtered = new java.util.ArrayList<>();
        for (Booking b : fetched) {
            java.time.LocalDateTime sched = b.getScheduledTime();
            if (sched == null) continue; // skip bookings without scheduled time when filtering

            java.time.LocalDate schedDate = sched.toLocalDate();
            boolean match = false;

            if (targetDate != null) {
                match = schedDate.equals(targetDate);
            } else if (year != null && month != null) {
                match = (schedDate.getYear() == year && schedDate.getMonthValue() == month);
            } else if (year != null) {
                match = (schedDate.getYear() == year);
            }

            if (match) filtered.add(b);
        }

        // Paginate filtered list
        int requestedPage = Math.max(1, page);
        int requestedLimit = Math.max(1, limit);
        int fromIndex = (requestedPage - 1) * requestedLimit;
        int toIndex = Math.min(fromIndex + requestedLimit, filtered.size());

        java.util.List<Booking> pageContent;
        if (fromIndex >= filtered.size()) {
            pageContent = java.util.Collections.emptyList();
        } else {
            pageContent = filtered.subList(fromIndex, toIndex);
        }

        org.springframework.data.domain.Page<Booking> resultPage =
                new org.springframework.data.domain.PageImpl<>(pageContent, createPageable(page, limit, sortType, sortBy), filtered.size());

        Page<BookingResponse> response = bookingMapper.mapToPage(resultPage, BookingResponse.class);
        return responseFactory.successPage(response, "Bookings retrieved successfully (filtered by date)");
    }

    @GetMapping
    @Operation(
            summary = "Admin: Get all bookings with optional status filter",
            description = "Admin endpoint to retrieve all bookings in the system. Can filter by status or get all bookings.",
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
    public ResponseEntity<PageResponse<BookingResponse>> getAllBookings(
            @Parameter(description = "Page number (1-based)")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "15") int limit,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "desc") String sortType,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Filter by booking status (optional)")
            @RequestParam(required = false) Constants.BookingStatusEnum status
    ) {
        Pageable pageable = createPageable(page, limit, sortType, sortBy);
        Page<Booking> bookings;

        if (status != null) {
            bookings = bookingService.getAllBookingsByStatus(status, pageable);
        } else {
            bookings = bookingService.getAllBookings(pageable);
        }

        Page<BookingResponse> response = bookingMapper.mapToPage(bookings, BookingResponse.class);
        return responseFactory.successPage(response, "All bookings retrieved successfully");
    }

    @GetMapping("/stat")
    @Operation(
            summary = "Admin: Get booking statistics",
            description = "Admin endpoint to get booking statistics (total, completed, pending, canceled)",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Statistics retrieved successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SingleResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Admin only",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<SingleResponse<BookingPageResponse.BookingStats>> getBookingStats() {
        BookingServiceImpl.BookingStats stats = ((BookingServiceImpl) bookingService).getAllBookingsStats();
        BookingPageResponse.BookingStats statsResponse = BookingPageResponse.BookingStats.builder()
                .totalBookings(stats.getTotalBookings())
                .completedBookings(stats.getCompletedBookings())
                .pendingBookings(stats.getPendingBookings())
                .canceledBookings(stats.getCanceledBookings())
                .build();
        return responseFactory.successSingle(statsResponse, "Booking statistics retrieved successfully");
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
    public ResponseEntity<SingleResponse<CreateBookingResponse>> createBooking(
            @Parameter(description = "Service package ID", required = true)
            @PathVariable UUID servicePackageId,
            @Parameter(description = "Booking data to create", required = true)
            @RequestBody @Valid BookingCreateRequest request
    ) {
        Booking createdBooking = bookingService.createBooking(request, servicePackageId);
        CreateBookingResponse response = bookingMapper.mapTo(createdBooking, CreateBookingResponse.class);
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
            @RequestParam(required = false) Constants.PaymentStatusEnum paymentStatus,
            @Parameter(description = "Filter by userId ")
            @RequestParam(required = false) UUID userId,
            @Parameter(description = "Filter by seerId ")
            @RequestParam(required = false) UUID seerId,
            @Parameter(description = "Filter by role ")
            @RequestParam(required = false) String role,
            @Parameter(description = "Search by user/seer name ")
            @RequestParam(required = false) String searchName
    ) {
        Pageable pageable = createPageable(page, limit, sortType, sortBy);
        Page<BookingPayment> payments;

        // Priority: searchName > userId > seerId > role > paymentMethod > paymentStatus > all
        if (searchName != null && !searchName.isBlank()) {
            payments = bookingService.findPaymentsByUserOrSeerName(searchName, pageable);
        } else if (userId != null) {
            payments = bookingService.findPaymentsByUserId(userId, pageable);
        } else if (seerId != null) {
            payments = bookingService.findPaymentsBySeerId(seerId, pageable);
        } else if (role != null && !role.isBlank()) {
            Constants.RoleEnum roleEnum = Constants.RoleEnum.get(role);
            payments = bookingService.findPaymentsByRole(roleEnum, pageable);
        } else if (paymentMethod != null) {
            payments = bookingService.findAllByPaymentMethod(paymentMethod, pageable);
        } else if (paymentStatus != null) {
            payments = bookingService.findAllByStatus(paymentStatus, pageable);
        } else {
            // If no filters provided, return all payments
            payments = bookingService.findAllBookingPayments(pageable);
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
    public ResponseEntity<String> paymentSuccess(
            @RequestParam("paymentId") String paymentId,
            @RequestParam("PayerID") String payerId
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

        String htmlContent = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>Payment Success</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <script>\n" +
                "        window.location.href = \"iseeyou://payment-success?bookingId=" + bookingPayment.getBooking().getId() + "\";\n" +
                "    </script>\n" +
                "    <p>Redirecting to app...</p>\n" +
                "</body>\n" +
                "</html>";

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(htmlContent);
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

    // New endpoint: Seer can view payments to their packages (optional packageId)
    @GetMapping("/seer/payments")
    @Operation(
            summary = "Seer: Get payments for your service packages (optional packageId)",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Payments retrieved successfully",
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
    @PreAuthorize("hasAuthority('SEER')")
    public ResponseEntity<PageResponse<BookingPaymentResponse>> seerGetPayments(
            @Parameter(description = "Service Package ID (optional)") @RequestParam(required = false) UUID packageId,
            @Parameter(description = "Page number (1-based)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "15") int limit,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortType,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy
    ) {
        Pageable pageable = createPageable(page, limit, sortType, sortBy);
        Page<BookingPayment> payments = bookingService.seerGetPayments(packageId, pageable);
        Page<BookingPaymentResponse> response = bookingMapper.mapToPage(payments, BookingPaymentResponse.class);
        return responseFactory.successPage(response, "Payments retrieved successfully");
    }

    // New endpoint: Customer can view payments created by themselves
    @GetMapping("/my-payments")
    @Operation(
            summary = "Customer: Get payments created by you",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Payments retrieved successfully",
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
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<PageResponse<BookingPaymentResponse>> userGetPayments(
            @Parameter(description = "Page number (1-based)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "15") int limit,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortType,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy
    ) {
        Pageable pageable = createPageable(page, limit, sortType, sortBy);
        Page<BookingPayment> payments = bookingService.userGetPayments(pageable);
        Page<BookingPaymentResponse> response = bookingMapper.mapToPage(payments, BookingPaymentResponse.class);
        return responseFactory.successPage(response, "Payments retrieved successfully");
    }

    // COMMENTED OUT: This endpoint is removed from API but the service method is kept for internal/automated use
    // The processPayment() method in BookingService can be called internally by the system
    // For example: scheduled jobs, automated payout processing, etc.

    /*
    @PostMapping("/pay")
    @Operation(
            summary = "Process payment for booking (Admin only)",
            description = "Process payment based on booking status: " +
                         "- If CANCELED: Refund to customer's PayPal account " +
                         "- If COMPLETED: Payout to seer's PayPal email (after deducting commission)",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Payment processed successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = BookingResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad request - Booking status invalid or payment already processed",
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
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Admin only",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<SingleResponse<BookingResponse>> processPayment(
            @Parameter(description = "Booking ID", required = true)
            @RequestParam UUID bookingId
    ) {
        log.info("Admin processing payment for booking {}", bookingId);
        Booking booking = bookingService.processPayment(bookingId);
        BookingResponse response = bookingMapper.mapTo(booking, BookingResponse.class);
        return responseFactory.successSingle(response, "Payment processed successfully");
    }
    */

    @PostMapping("/{id}/seer-confirm")
    @Operation(
            summary = "Seer confirm or cancel a booking (Seer only)",
            description = "Seer can confirm or cancel a booking. If seer cancels, refund will be processed if payment completed.",
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
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    @PreAuthorize("hasAuthority('SEER')")
    public ResponseEntity<SingleResponse<BookingResponse>> seerConfirmBooking(
            @Parameter(description = "Booking ID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Seer action (CONFIRMED or CANCELED)", required = true)
            @RequestBody @Valid BookingSeerConfirmRequest request
    ) {
        Booking updatedBooking = bookingService.seerConfirmBooking(id, request.getStatus());
        BookingResponse response = bookingMapper.mapTo(updatedBooking, BookingResponse.class);
        return responseFactory.successSingle(response, "Booking updated successfully");
    }

    // New: Admin endpoint to get booking reviews with filters (packageId, seerId)
    @GetMapping("/reviews")
    @Operation(
            summary = "Admin: Get booking reviews with optional filters (packageId, seerId)",
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
    public ResponseEntity<PageResponse<BookingReviewResponse>> adminGetReviews(
            @Parameter(description = "Service Package ID") @RequestParam(required = false) UUID packageId,
            @Parameter(description = "Seer ID") @RequestParam(required = false) UUID seerId,
            @Parameter(description = "Page number (1-based)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "15") int limit,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortType,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "reviewedAt") String sortBy
    ) {
        Pageable pageable = createPageable(page, limit, sortType, sortBy);
        Page<BookingReviewResponse> response = bookingService.adminGetReviews(packageId, seerId, pageable);
        return responseFactory.successPage(response, "Reviews retrieved successfully");
    }

    // New: Seer endpoint to get reviews for packages owned by the seer (optional packageId filter)
    @GetMapping("/seer/reviews")
    @Operation(
            summary = "Seer: Get booking reviews for your service packages (optional packageId)",
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
    @PreAuthorize("hasAuthority('SEER')")
    public ResponseEntity<PageResponse<BookingReviewResponse>> seerGetReviews(
            @Parameter(description = "Service Package ID") @RequestParam(required = false) UUID packageId,
            @Parameter(description = "Page number (1-based)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "15") int limit,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortType,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "reviewedAt") String sortBy
    ) {
        Pageable pageable = createPageable(page, limit, sortType, sortBy);
        Page<BookingReviewResponse> response = bookingService.seerGetReviews(packageId, pageable);
        return responseFactory.successPage(response, "Reviews retrieved successfully");
    }

    @GetMapping("/revenue/daily")
    @Operation(
            summary = "Get daily revenue (Admin only)",
            description = "Tính tổng doanh thu trong ngày = (PAID_PACKAGE - RECEIVED_PACKAGE) với status COMPLETED. Trả về thông tin doanh thu và thuế cố định 10%",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Daily revenue retrieved successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SingleResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad request - Invalid date format",
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
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Admin access required",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<SingleResponse<DailyRevenueResponse>> getDailyRevenue(
            @Parameter(description = "Date to calculate revenue (format: yyyy-MM-dd). Default is today", example = "2025-11-08")
            @RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate date
    ) {
        // Nếu không truyền date, lấy ngày hôm nay
        java.time.LocalDate targetDate = date != null ? date : java.time.LocalDate.now();

        DailyRevenueResponse revenue = bookingService.getDailyRevenue(targetDate);
        return responseFactory.successSingle(revenue, "Daily revenue retrieved successfully");
    }

    @GetMapping("/payment/my-seer-salary")
    @Operation(
            summary = "Seer: Get my salary history (RECEIVED_PACKAGE and BONUS payments)",
            description = "Seer xem lịch sử nhận lương của mình. Bao gồm payments có type RECEIVED_PACKAGE (từ bookings) và BONUS. " +
                    "Hỗ trợ lọc theo payment type, status, và ngày/tháng/năm.",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Salary history retrieved successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = PageResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad request - Invalid parameters",
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
    @PreAuthorize("hasAuthority('SEER')")
    public ResponseEntity<PageResponse<BookingPaymentResponse>> getMySeerSalary(
            @Parameter(description = "Filter by payment type (RECEIVED_PACKAGE or BONUS)")
            @RequestParam(required = false) Constants.PaymentTypeEnum paymentType,
            @Parameter(description = "Filter by payment status")
            @RequestParam(required = false) Constants.PaymentStatusEnum paymentStatus,
            @Parameter(description = "Filter by year (optional)")
            @RequestParam(required = false) Integer year,
            @Parameter(description = "Filter by month (1-12, optional)")
            @RequestParam(required = false) Integer month,
            @Parameter(description = "Filter by day (1-31, optional)")
            @RequestParam(required = false) Integer day,
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
        Page<BookingPayment> salaryPayments = bookingService.getMySeerSalary(
                paymentType, paymentStatus, year, month, day, pageable
        );
        Page<BookingPaymentResponse> response = bookingMapper.mapToPage(salaryPayments, BookingPaymentResponse.class);
        return responseFactory.successPage(response, "Seer salary history retrieved successfully");
    }

    @GetMapping("/payments/seer-salary")
    @Operation(
            summary = "Admin: Get all seer salary history (RECEIVED_PACKAGE and BONUS payments)",
            description = "Admin xem tất cả lịch sử lương đã phát cho các seer. Bao gồm payments có type RECEIVED_PACKAGE và BONUS. " +
                    "Hỗ trợ lọc theo payment type, status, và ngày/tháng/năm.",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "All seer salary history retrieved successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = PageResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad request - Invalid parameters",
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
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Admin only",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<PageResponse<BookingPaymentResponse>> getAllSeerSalary(
            @Parameter(description = "Filter by payment type (RECEIVED_PACKAGE or BONUS)")
            @RequestParam(required = false) Constants.PaymentTypeEnum paymentType,
            @Parameter(description = "Filter by payment status")
            @RequestParam(required = false) Constants.PaymentStatusEnum paymentStatus,
            @Parameter(description = "Filter by year (optional)")
            @RequestParam(required = false) Integer year,
            @Parameter(description = "Filter by month (1-12, optional)")
            @RequestParam(required = false) Integer month,
            @Parameter(description = "Filter by day (1-31, optional)")
            @RequestParam(required = false) Integer day,
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
        Page<BookingPayment> salaryPayments = bookingService.getAllSeerSalary(
                paymentType, paymentStatus, year, month, day, pageable
        );
        Page<BookingPaymentResponse> response = bookingMapper.mapToPage(salaryPayments, BookingPaymentResponse.class);
        return responseFactory.successPage(response, "All seer salary history retrieved successfully");
    }

    @GetMapping("/payment/stats")
    @Operation(
            summary = "Admin: Get booking payment statistics",
            description = "Lấy thống kê tổng quan về các giao dịch thanh toán cho admin. Bao gồm: " +
                    "tổng doanh thu (phí dịch vụ), số giao dịch thành công, số giao dịch bị hoàn tiền, và tổng số tiền đã hoàn lại.",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Payment statistics retrieved successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SingleResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Only admin can access",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<SingleResponse<com.iseeyou.fortunetelling.dto.response.booking.BookingPaymentStatsResponse>> getPaymentStats() {
        com.iseeyou.fortunetelling.dto.response.booking.BookingPaymentStatsResponse stats = bookingService.getPaymentStats();
        return responseFactory.successSingle(stats, "Payment statistics retrieved successfully");
    }
}
