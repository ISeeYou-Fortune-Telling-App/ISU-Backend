package com.iseeyou.fortunetelling.service.booking.impl;

import com.iseeyou.fortunetelling.dto.request.booking.BookingCreateRequest;
import com.iseeyou.fortunetelling.dto.request.booking.BookingReviewRequest;
import com.iseeyou.fortunetelling.dto.request.booking.BookingUpdateRequest;
import com.iseeyou.fortunetelling.dto.response.booking.BookingReviewResponse;
import com.iseeyou.fortunetelling.dto.response.booking.DailyRevenueResponse;
import com.iseeyou.fortunetelling.entity.booking.Booking;
import com.iseeyou.fortunetelling.entity.booking.BookingPayment;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.exception.NotFoundException;
import com.iseeyou.fortunetelling.mapper.BookingMapper;
import com.iseeyou.fortunetelling.repository.booking.BookingPaymentRepository;
import com.iseeyou.fortunetelling.repository.booking.BookingRepository;
import com.iseeyou.fortunetelling.service.booking.BookingService;
import com.iseeyou.fortunetelling.service.booking.strategy.PaymentStrategy;
import com.iseeyou.fortunetelling.service.chat.ConversationService;
import com.iseeyou.fortunetelling.service.servicepackage.ServicePackageService;
import com.iseeyou.fortunetelling.service.user.UserService;
import com.iseeyou.fortunetelling.service.booking.strategy.gateway.PayPalGateway;
import com.iseeyou.fortunetelling.util.Constants;
import com.paypal.base.rest.PayPalRESTException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.JoinType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BookingPaymentRepository bookingPaymentRepository;
    private final UserService userService;
    private final ServicePackageService servicePackageService;
    private final ConversationService conversationService;
    private final BookingMapper bookingMapper;
    private final Map<Constants.PaymentMethodEnum, PaymentStrategy> paymentStrategies;
    private final PayPalGateway payPalGateway;

    @Override
    @Transactional(readOnly = true)
    public Page<Booking> getBookingsByMe(Pageable pageable) {
        User currentUser = userService.getUser();
        // Get bookings where user is either customer or seer (for all roles including ADMIN)
        return bookingRepository.findAllByUserAsCustomerOrSeer(currentUser, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Booking> getBookingsByMeAndStatus(Constants.BookingStatusEnum status, Pageable pageable) {
        User currentUser = userService.getUser();
        // Get bookings where user is either customer or seer with status filter (for all roles including ADMIN)
        return bookingRepository.findAllByUserAsCustomerOrSeerAndStatus(currentUser, status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Booking> getAllBookings(Pageable pageable) {
        return bookingRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Booking> getAllBookingsByStatus(Constants.BookingStatusEnum status, Pageable pageable) {
        return bookingRepository.findAllByStatus(status, pageable);
    }

    @Transactional(readOnly = true)
    public BookingStats getAllBookingsStats() {
        long total = bookingRepository.count();
        long completed = bookingRepository.countByStatus(Constants.BookingStatusEnum.COMPLETED);
        long pending = bookingRepository.countByStatus(Constants.BookingStatusEnum.PENDING);
        long canceled = bookingRepository.countByStatus(Constants.BookingStatusEnum.CANCELED);
        
        return BookingStats.builder()
                .totalBookings(total)
                .completedBookings(completed)
                .pendingBookings(pending)
                .canceledBookings(canceled)
                .build();
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class BookingStats {
        private Long totalBookings;
        private Long completedBookings;
        private Long pendingBookings;
        private Long canceledBookings;
    }

    @Override
    @Transactional(readOnly = true)
    public Booking findById(UUID id) {
        return bookingRepository.findWithDetailById(id)
                .orElseThrow(() -> new NotFoundException("Booking not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingPayment> findAllByPaymentMethod(Constants.PaymentMethodEnum paymentMethodEnum, Pageable pageable) {
        return bookingPaymentRepository.findAllByPaymentMethod(paymentMethodEnum, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingPayment> findAllByStatus(Constants.PaymentStatusEnum statusEnum, Pageable pageable) {
        return bookingPaymentRepository.findAllByStatus(statusEnum, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingPayment> findAllBookingPayments(Pageable pageable) {
        return bookingPaymentRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingPayment findPaymentById(UUID id) {
        return bookingPaymentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("BookingPayment not found with id: " + id));
    }

    @Override
    @Transactional
    public Booking createBooking(BookingCreateRequest request, UUID packageId) {
        // Validate that only PayPal is supported temporarily
        if (request.getPaymentMethod() != Constants.PaymentMethodEnum.PAYPAL) {
            throw new IllegalArgumentException("Currently only PayPal payment method is supported. Please use PAYPAL.");
        }
        
        // Map DTO to Entity
        Booking booking = bookingMapper.mapTo(request, Booking.class);

        // Set business logic fields
        booking.setStatus(Constants.BookingStatusEnum.PENDING);
        booking.setServicePackage(servicePackageService.findById(packageId.toString()));
        User customer = userService.getUser();
        booking.setCustomer(customer);

        // Kiểm tra schedule time hợp lệ với available time của service package
        if (request.getScheduledTime() != null) {
            validateScheduledTimeWithAvailableTime(packageId, request.getScheduledTime());
        }

        Booking newBooking = bookingRepository.save(booking);

        try {
            BookingPayment bookingPayment = createBookingPayment(newBooking, request.getPaymentMethod());
            newBooking.getBookingPayments().add(bookingPayment);
            bookingRepository.save(newBooking);
        } catch (PayPalRESTException e) {
            log.error("Error creating PayPal payment: {}", e.getMessage());
            newBooking.setStatus(Constants.BookingStatusEnum.FAILED);
            bookingRepository.save(newBooking);
            throw new RuntimeException("Error creating payment", e);
        }

        // TODO: Create conversation

        // Fetch booking with all relationships to avoid LazyInitializationException
        return bookingRepository.findWithDetailById(newBooking.getId())
                .orElseThrow(() -> new NotFoundException("Booking not found with id: " + newBooking.getId()));
    }

    /**
     * Kiểm tra xem thời gian đặt lịch có hợp lệ với available time của service package không
     */
    private void validateScheduledTimeWithAvailableTime(UUID packageId, LocalDateTime scheduledTime) {
        // Lấy thứ trong tuần từ scheduled time (Monday = 1, Sunday = 7)
        int dayOfWeek = scheduledTime.getDayOfWeek().getValue(); // 1-7

        // Convert sang format của hệ thống (2-8: Thứ 2 - Chủ nhật)
        int weekDate = dayOfWeek == 7 ? 8 : dayOfWeek + 1;

        // Lấy giờ từ scheduled time
        java.time.LocalTime scheduledTimeOnly = scheduledTime.toLocalTime();

        // Kiểm tra xem thời gian có nằm trong available time không
        boolean isAvailable = servicePackageService.isTimeAvailable(packageId, weekDate, scheduledTimeOnly);

        if (!isAvailable) {
            throw new IllegalArgumentException(
                String.format("Thời gian đặt lịch %s không nằm trong khung giờ rảnh của dịch vụ. " +
                    "Vui lòng chọn thời gian khác hoặc xem thông tin available time của gói dịch vụ.",
                    scheduledTime)
            );
        }

        log.info("Validated scheduled time {} for package {}: OK", scheduledTime, packageId);
    }

    @Override
    @Transactional
    public BookingPayment executePayment(Constants.PaymentMethodEnum paymentMethod, Map<String, Object> paymentParams) {
        PaymentStrategy paymentStrategy = paymentStrategies.get(paymentMethod);
        if (paymentStrategy == null) {
            throw new IllegalArgumentException("Unsupported payment method: " + paymentMethod);
        }
        return paymentStrategy.executePayment(paymentParams);
    }

    @Transactional
    protected BookingPayment createBookingPayment(Booking booking, Constants.PaymentMethodEnum paymentMethod) throws PayPalRESTException {
        PaymentStrategy paymentStrategy = paymentStrategies.get(paymentMethod);
        if (paymentStrategy == null) {
            throw new IllegalArgumentException("Unsupported payment method: " + paymentMethod);
        }
        return paymentStrategy.pay(booking);
    }

    @Override
    @Transactional
    public Booking updateBooking(UUID id, BookingUpdateRequest request) {
        Booking existingBooking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking not found with id: " + id));

        // TODO: Check if changed to other time => update conversation

        // check if status changed to CONFIRMED
        boolean statusChangedToConfirmed = request.getStatus() != null &&
                !existingBooking.getStatus().equals(request.getStatus()) &&
                request.getStatus().equals(Constants.BookingStatusEnum.CONFIRMED);

        // Update booking fields from request
        if (request.getStatus() != null) {
            existingBooking.setStatus(request.getStatus());
        }
        if (request.getAdditionalNote() != null) {
            existingBooking.setAdditionalNote(request.getAdditionalNote());
        }
        if (request.getScheduledTime() != null) {
            existingBooking.setScheduledTime(request.getScheduledTime());
        }

        bookingRepository.save(existingBooking);

        // create chat session if booking confirmed
        if (statusChangedToConfirmed) {
            log.info("Booking confirmed, creating chat session for booking: {}", id);
            conversationService.createChatSession(id);
        }

        // Fetch booking with all relationships to avoid LazyInitializationException
        Booking updatedBooking = bookingRepository.findWithDetailById(id)
                .orElseThrow(() -> new NotFoundException("Booking not found with id: " + id));

        return updatedBooking;
    }

    @Override
    @Transactional
    public void deleteBooking(UUID id) {
        Booking existingBooking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking not found with id: " + id));
        bookingRepository.delete(existingBooking);
    }

    @Override
    @Transactional
    public Booking cancelBooking(UUID id) {
        log.info("Starting cancel booking process for booking {}", id);

        // 1. Find booking
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking not found with id: " + id));

        // 2. Validate booking status - can only cancel PENDING or CONFIRMED bookings
        if (booking.getStatus() == Constants.BookingStatusEnum.CANCELED) {
            throw new IllegalArgumentException("Booking is already cancelled");
        }

        if (booking.getStatus() == Constants.BookingStatusEnum.COMPLETED) {
            throw new IllegalArgumentException("Cannot cancel a completed booking");
        }

        if (booking.getStatus() == Constants.BookingStatusEnum.FAILED) {
            throw new IllegalArgumentException("Cannot cancel a failed booking");
        }

        // 3. Validate user permission - only customer can cancel their own booking
        User currentUser = userService.getUser();
        boolean isCustomer = booking.getCustomer().getId().equals(currentUser.getId());

        if (!isCustomer) {
            throw new IllegalArgumentException("Only the booking customer can cancel this booking");
        }

        // 4. Validate cancellation time - must be at least 2 hours before scheduled time
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime scheduledTime = booking.getScheduledTime();

        if (scheduledTime == null) {
            log.warn("Booking {} has no scheduled time. Allowing cancellation.", id);
        } else {
            LocalDateTime cancellationDeadline = scheduledTime.minusHours(2);

            if (now.isAfter(cancellationDeadline)) {
                long hoursUntilScheduled = java.time.Duration.between(now, scheduledTime).toHours();
                throw new IllegalArgumentException(
                    String.format("Cannot cancel booking less than 2 hours before scheduled time. " +
                                "Your booking is scheduled for %s (in %d hours). " +
                                "Cancellation deadline was %s.",
                                scheduledTime.toString(),
                                hoursUntilScheduled,
                                cancellationDeadline.toString())
                );
            }

            log.info("Cancellation allowed. Current time: {}, Scheduled time: {}, Deadline: {}",
                    now, scheduledTime, cancellationDeadline);
        }

        // 5. Check if there's a completed payment to refund
        BookingPayment completedPayment = booking.getBookingPayments().stream()
                .filter(p -> p.getStatus().equals(Constants.PaymentStatusEnum.COMPLETED))
                .findFirst()
                .orElse(null);

        if (completedPayment != null) {
            log.info("Found completed payment {} for booking {}. Processing refund.",
                    completedPayment.getId(), id);

            // Process refund through payment strategy
            try {
                PaymentStrategy paymentStrategy = paymentStrategies.get(completedPayment.getPaymentMethod());

                if (paymentStrategy == null) {
                    throw new IllegalArgumentException(
                        "Payment method not supported for refund: " + completedPayment.getPaymentMethod()
                    );
                }

                // Call strategy to refund payment
                BookingPayment refundedPayment = paymentStrategy.refund(id, completedPayment);
                log.info("Payment {} refunded successfully", refundedPayment.getId());

            } catch (Exception e) {
                log.error("Failed to refund payment for booking {}: {}", id, e.getMessage(), e);
                throw new RuntimeException(
                    "Cancellation failed: Unable to process refund. " + e.getMessage(), e
                );
            }
        } else {
            log.info("No completed payment found for booking {}. No refund needed.", id);
        }

        // 6. Update booking status to CANCELED
        booking.setStatus(Constants.BookingStatusEnum.CANCELED);
        bookingRepository.save(booking);

        log.info("Booking {} cancelled successfully by user {}", id, currentUser.getId());

        // TODO: Send push notification to seer about booking cancellation
        // This will notify the seer that the customer has cancelled the booking
        // Implementation pending: Push notification service integration
        // Expected payload: {
        //   "type": "BOOKING_CANCELLED",
        //   "bookingId": id,
        //   "customerId": currentUser.getId(),
        //   "customerName": currentUser.getFullName(),
        //   "seerId": booking.getServicePackage().getSeer().getId(),
        //   "scheduledTime": scheduledTime,
        //   "message": "Customer {customerName} has cancelled booking {bookingId} scheduled for {scheduledTime}"
        // }

        // 7. Fetch booking with all relationships to avoid LazyInitializationException
        Booking cancelledBooking = bookingRepository.findWithDetailById(id)
                .orElseThrow(() -> new NotFoundException("Booking not found with id: " + id));

        return cancelledBooking;
    }

    @Override
    @Transactional
    public Booking refundBooking(UUID id) {
        log.info("Starting refund process for booking {}", id);

        // 1. Find booking with payments
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking not found with id: " + id));

        // 2. Validate booking status - cannot refund already cancelled or completed bookings
        if (booking.getStatus().equals(Constants.BookingStatusEnum.CANCELED)) {
            throw new IllegalArgumentException("Booking is already cancelled. Cannot refund.");
        }

        if (booking.getStatus().equals(Constants.BookingStatusEnum.COMPLETED)) {
            throw new IllegalArgumentException("Cannot refund a completed booking. Please contact support.");
        }

        if (booking.getStatus().equals(Constants.BookingStatusEnum.FAILED)) {
            throw new IllegalArgumentException("Cannot refund a failed booking.");
        }

        // 3. Validate user permission - only customer can refund their own booking (or admin)
        User currentUser = userService.getUser();
        boolean isCustomer = booking.getCustomer().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole().equals(Constants.RoleEnum.ADMIN);

        if (!isCustomer && !isAdmin) {
            throw new IllegalArgumentException("Only the booking customer or admin can request a refund");
        }

        // 4. Find completed payment to refund
        BookingPayment completedPayment = booking.getBookingPayments().stream()
                .filter(p -> p.getStatus().equals(Constants.PaymentStatusEnum.COMPLETED))
                .findFirst()
                .orElse(null);

        if (completedPayment == null) {
            log.warn("No completed payment found for booking {}", id);
            throw new IllegalArgumentException("No completed payment found for this booking. Nothing to refund.");
        }

        // 5. Check if payment was already refunded
        boolean hasRefundedPayment = booking.getBookingPayments().stream()
                .anyMatch(p -> p.getStatus().equals(Constants.PaymentStatusEnum.REFUNDED));

        if (hasRefundedPayment) {
            throw new IllegalArgumentException("This booking has already been refunded");
        }

        // 6. Process refund through payment strategy
        try {
            PaymentStrategy paymentStrategy = paymentStrategies.get(completedPayment.getPaymentMethod());

            if (paymentStrategy == null) {
                throw new IllegalArgumentException(
                    "Payment method not supported for refund: " + completedPayment.getPaymentMethod()
                );
            }

            log.info("Processing refund for payment {} using {} strategy",
                    completedPayment.getId(), completedPayment.getPaymentMethod());

            // Call strategy to refund payment
            BookingPayment refundedPayment = paymentStrategy.refund(id, completedPayment);

            // 7. Update booking status to CANCELED
            booking.setStatus(Constants.BookingStatusEnum.CANCELED);
            bookingRepository.save(booking);

            log.info("Booking {} refunded successfully. Payment {} status: REFUNDED",
                    id, refundedPayment.getId());

            // 8. Fetch booking with all relationships to avoid LazyInitializationException
            Booking updatedBooking = bookingRepository.findWithDetailById(id)
                    .orElseThrow(() -> new NotFoundException("Booking not found with id: " + id));

            return updatedBooking;

        } catch (IllegalArgumentException e) {
            // These are validation errors with clear messages - pass them through
            log.error("Refund validation failed for booking {}: {}", id, e.getMessage());
            throw e;
        } catch (PayPalRESTException e) {
            log.error("PayPal refund processing failed for booking {}: {} - Details: {}",
                    id, e.getMessage(), e.getDetails(), e);

            // Provide more specific error messages based on PayPal error codes
            String errorMessage = buildRefundErrorMessage(id, completedPayment, e);
            throw new RuntimeException(errorMessage, e);
        } catch (UnsupportedOperationException e) {
            log.error("Refund not supported for booking {}: {}", id, e.getMessage());
            throw new IllegalArgumentException(e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error during refund for booking {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Unexpected error during refund processing. Please contact support with booking ID: " + id, e);
        }
    }

    @Override
    @Transactional
    public BookingReviewResponse submitReview(UUID bookingId, BookingReviewRequest reviewRequest) {
        User currentUser = userService.getUser();
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found with id: " + bookingId));

        // Validate: Only customer of the booking can review
        if (!booking.getCustomer().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Only the customer of this booking can submit a review");
        }

        // Validate: Booking must be COMPLETED
        if (!booking.getStatus().equals(Constants.BookingStatusEnum.COMPLETED)) {
            throw new IllegalArgumentException("Can only review completed bookings. Current status: " + booking.getStatus());
        }

        // Validate: Cannot review twice
        if (booking.getRating() != null) {
            throw new IllegalArgumentException("This booking has already been reviewed");
        }

        // Set review data
        booking.setRating(reviewRequest.getRating());
        booking.setComment(reviewRequest.getComment());
        booking.setReviewedAt(LocalDateTime.now());

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Review submitted for booking {} by user {}", bookingId, currentUser.getId());

        // Build response
        return BookingReviewResponse.builder()
                .bookingId(savedBooking.getId())
                .rating(savedBooking.getRating())
                .comment(savedBooking.getComment())
                .reviewedAt(savedBooking.getReviewedAt())
                .customer(BookingReviewResponse.CustomerInfo.builder()
                        .customerId(savedBooking.getCustomer().getId())
                        .customerName(savedBooking.getCustomer().getFullName())
                        .customerAvatar(savedBooking.getCustomer().getAvatarUrl())
                        .build())
                .servicePackage(BookingReviewResponse.ServicePackageInfo.builder()
                        .packageId(savedBooking.getServicePackage().getId())
                        .packageTitle(savedBooking.getServicePackage().getPackageTitle())
                        .build())
                .build();
    }

    @Override
    @Transactional
    public Booking seerConfirmBooking(UUID id, Constants.BookingStatusEnum status) {
        log.info("Seer action {} on booking {}", status, id);

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking not found with id: " + id));

        User currentUser = userService.getUser();

        // Only seer assigned to the service package can perform this action
        if (!currentUser.getRole().equals(Constants.RoleEnum.SEER)) {
            throw new IllegalArgumentException("Only a seer can perform this action");
        }

        if (booking.getServicePackage() == null || booking.getServicePackage().getSeer() == null ||
                !booking.getServicePackage().getSeer().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("You are not authorized to modify this booking");
        }

        if (status == null) {
            throw new IllegalArgumentException("Status is required");
        }

        if (status.equals(Constants.BookingStatusEnum.CONFIRMED)) {
            // Allow confirming a pending booking. If already confirmed, just return.
            if (booking.getStatus().equals(Constants.BookingStatusEnum.CONFIRMED)) {
                log.info("Booking {} already confirmed", id);
            } else if (booking.getStatus().equals(Constants.BookingStatusEnum.CANCELED)
                    || booking.getStatus().equals(Constants.BookingStatusEnum.COMPLETED)
                    || booking.getStatus().equals(Constants.BookingStatusEnum.FAILED)) {
                throw new IllegalArgumentException("Cannot confirm booking with status: " + booking.getStatus());
            } else {
                booking.setStatus(Constants.BookingStatusEnum.CONFIRMED);
                bookingRepository.save(booking);
                log.info("Booking {} confirmed by seer {}", id, currentUser.getId());

                // Note: Chat creation is handled elsewhere when it's time; we only change DB status here.
            }

        } else if (status.equals(Constants.BookingStatusEnum.CANCELED)) {
            // Seer-initiated cancel -> refund if payment exists
            if (booking.getStatus().equals(Constants.BookingStatusEnum.CANCELED)) {
                throw new IllegalArgumentException("Booking is already cancelled");
            }

            if (booking.getStatus().equals(Constants.BookingStatusEnum.COMPLETED)) {
                throw new IllegalArgumentException("Cannot cancel a completed booking");
            }

            if (booking.getStatus().equals(Constants.BookingStatusEnum.FAILED)) {
                throw new IllegalArgumentException("Cannot cancel a failed booking");
            }

            // Find a completed payment
            BookingPayment completedPayment = booking.getBookingPayments().stream()
                    .filter(p -> p.getStatus().equals(Constants.PaymentStatusEnum.COMPLETED))
                    .findFirst()
                    .orElse(null);

            if (completedPayment != null) {
                try {
                    PaymentStrategy paymentStrategy = paymentStrategies.get(completedPayment.getPaymentMethod());
                    if (paymentStrategy == null) {
                        throw new IllegalArgumentException("Payment method not supported for refund: " + completedPayment.getPaymentMethod());
                    }
                    BookingPayment refunded = paymentStrategy.refund(id, completedPayment);
                    log.info("Refund processed for booking {} payment {}", id, refunded.getId());
                } catch (Exception e) {
                    log.error("Failed to refund payment for booking {}: {}", id, e.getMessage(), e);
                    throw new RuntimeException("Cancellation failed: Unable to process refund. " + e.getMessage(), e);
                }
            } else {
                log.info("No completed payment found for booking {}. No refund needed.", id);
            }

            booking.setStatus(Constants.BookingStatusEnum.CANCELED);
            bookingRepository.save(booking);
            log.info("Booking {} cancelled by seer {}", id, currentUser.getId());

        } else {
            throw new IllegalArgumentException("Invalid status for seer action. Only CONFIRMED or CANCELED are allowed");
        }

        Booking updated = bookingRepository.findWithDetailById(id)
                .orElseThrow(() -> new NotFoundException("Booking not found with id: " + id));

        return updated;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingReviewResponse> getReviewsByServicePackage(UUID packageId, Pageable pageable) {
        Page<Booking> bookingsWithReviews = bookingRepository.findReviewsByServicePackageId(packageId, pageable);

        return bookingsWithReviews.map(booking -> BookingReviewResponse.builder()
                .bookingId(booking.getId())
                .rating(booking.getRating())
                .comment(booking.getComment())
                .reviewedAt(booking.getReviewedAt())
                .customer(BookingReviewResponse.CustomerInfo.builder()
                        .customerId(booking.getCustomer().getId())
                        .customerName(booking.getCustomer().getFullName())
                        .customerAvatar(booking.getCustomer().getAvatarUrl())
                        .build())
                .servicePackage(BookingReviewResponse.ServicePackageInfo.builder()
                        .packageId(booking.getServicePackage().getId())
                        .packageTitle(booking.getServicePackage().getPackageTitle())
                        .build())
                .build());
    }

    // New: admin can filter reviews by packageId and/or seerId
    @Override
    @Transactional(readOnly = true)
    public Page<BookingReviewResponse> adminGetReviews(UUID packageId, UUID seerId, Pageable pageable) {
        // Ensure user is admin
        User currentUser = userService.getUser();
        if (!currentUser.getRole().equals(Constants.RoleEnum.ADMIN)) {
            throw new IllegalArgumentException("Only admin can access this endpoint");
        }

        Page<Booking> bookings = bookingRepository.findReviewsByFilters(packageId, seerId, pageable);

        return bookings.map(booking -> BookingReviewResponse.builder()
                .bookingId(booking.getId())
                .rating(booking.getRating())
                .comment(booking.getComment())
                .reviewedAt(booking.getReviewedAt())
                .customer(BookingReviewResponse.CustomerInfo.builder()
                        .customerId(booking.getCustomer().getId())
                        .customerName(booking.getCustomer().getFullName())
                        .customerAvatar(booking.getCustomer().getAvatarUrl())
                        .build())
                .servicePackage(BookingReviewResponse.ServicePackageInfo.builder()
                        .packageId(booking.getServicePackage().getId())
                        .packageTitle(booking.getServicePackage().getPackageTitle())
                        .build())
                .build());
    }

    // New: seer can get reviews for their own packages, optional filter by packageId
    @Override
    @Transactional(readOnly = true)
    public Page<BookingReviewResponse> seerGetReviews(UUID packageId, Pageable pageable) {
        User currentUser = userService.getUser();
        if (!currentUser.getRole().equals(Constants.RoleEnum.SEER)) {
            throw new IllegalArgumentException("Only seer can access this endpoint");
        }

        UUID seerId = currentUser.getId();
        Page<Booking> bookings = bookingRepository.findReviewsByFilters(packageId, seerId, pageable);

        return bookings.map(booking -> BookingReviewResponse.builder()
                .bookingId(booking.getId())
                .rating(booking.getRating())
                .comment(booking.getComment())
                .reviewedAt(booking.getReviewedAt())
                .customer(BookingReviewResponse.CustomerInfo.builder()
                        .customerId(booking.getCustomer().getId())
                        .customerName(booking.getCustomer().getFullName())
                        .customerAvatar(booking.getCustomer().getAvatarUrl())
                        .build())
                .servicePackage(BookingReviewResponse.ServicePackageInfo.builder()
                        .packageId(booking.getServicePackage().getId())
                        .packageTitle(booking.getServicePackage().getPackageTitle())
                        .build())
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingPayment> findPaymentsWithInvalidTransactionIds(Pageable pageable) {
        log.info("Searching for payments with invalid transaction IDs");

        // Get all payments and filter for invalid ones
        Page<BookingPayment> allPayments = bookingPaymentRepository.findAll(pageable);

        // Filter in-memory for invalid transaction IDs
        // Note: This is not optimal for large datasets, but suitable for admin debugging
        java.util.List<BookingPayment> invalidPayments = allPayments.getContent().stream()
            .filter(payment -> isInvalidPayment(payment))
            .collect(java.util.stream.Collectors.toList());

        log.info("Found {} payments with invalid transaction IDs out of {} total",
                invalidPayments.size(), allPayments.getContent().size());

        return new org.springframework.data.domain.PageImpl<>(
            invalidPayments,
            pageable,
            invalidPayments.size()
        );
    }

    // New: Seer can view payments to their service packages (optional packageId)
    @Override
    @Transactional(readOnly = true)
    public Page<BookingPayment> seerGetPayments(UUID packageId, Pageable pageable) {
        User currentUser = userService.getUser();
        if (!currentUser.getRole().equals(Constants.RoleEnum.SEER)) {
            throw new IllegalArgumentException("Only seer can access this endpoint");
        }

        UUID seerId = currentUser.getId();
        if (packageId != null) {
            return bookingPaymentRepository.findAllByBooking_ServicePackage_IdAndBooking_ServicePackage_Seer_Id(packageId, seerId, pageable);
        } else {
            return bookingPaymentRepository.findAllByBooking_ServicePackage_Seer_Id(seerId, pageable);
        }
    }

    // New: User can view payments created by themselves
    @Override
    @Transactional(readOnly = true)
    public Page<BookingPayment> userGetPayments(Pageable pageable) {
        User currentUser = userService.getUser();
        if (!currentUser.getRole().equals(Constants.RoleEnum.CUSTOMER)) {
            throw new IllegalArgumentException("Only customer can access this endpoint");
        }

        UUID customerId = currentUser.getId();
        return bookingPaymentRepository.findAllByBooking_Customer_Id(customerId, pageable);
    }

    /**
     * Check if a payment has invalid transaction ID
     */
    private boolean isInvalidPayment(BookingPayment payment) {
        // Check if completed/refunded payment is missing transaction ID
        if ((payment.getStatus() == Constants.PaymentStatusEnum.COMPLETED ||
             payment.getStatus() == Constants.PaymentStatusEnum.REFUNDED) &&
            (payment.getTransactionId() == null || payment.getTransactionId().trim().isEmpty())) {
            log.debug("Payment {} has status {} but missing transaction ID",
                    payment.getId(), payment.getStatus());
            return true;
        }

        // Check if PayPal payment has invalid transaction ID format
        if (payment.getPaymentMethod() == Constants.PaymentMethodEnum.PAYPAL &&
            payment.getTransactionId() != null &&
            !payment.getTransactionId().trim().isEmpty()) {

            String txnId = payment.getTransactionId();
            // Check for invalid prefixes indicating wrong payment method
            if (txnId.toUpperCase().startsWith("MOMO_") ||
                txnId.toUpperCase().startsWith("VNPAY_")) {
                log.debug("PayPal payment {} has invalid transaction ID with wrong prefix: {}",
                        payment.getId(), txnId);
                return true;
            }

            // Check for other invalid patterns
            if (txnId.length() < 5 || txnId.length() > 100) {
                log.debug("PayPal payment {} has invalid transaction ID length: {}",
                        payment.getId(), txnId.length());
                return true;
            }
        }

        return false;
    }

    /**
     * Build a user-friendly error message for refund failures
     */
    private String buildRefundErrorMessage(UUID bookingId, BookingPayment payment, PayPalRESTException e) {
        StringBuilder message = new StringBuilder();
        message.append("Refund failed for booking ").append(bookingId).append(". ");

        // Get error details as string for checking
        String errorDetails = e.getDetails() != null ? e.getDetails().toString() : "";
        String errorMessage = e.getMessage() != null ? e.getMessage() : "";

        // Check for specific PayPal error codes
        if (errorDetails.contains("INVALID_RESOURCE_ID") || errorMessage.contains("INVALID_RESOURCE_ID")) {
            message.append("The payment transaction could not be found in PayPal system. ");
            message.append("This may occur if: ");
            message.append("(1) The payment was created with a different payment gateway, ");
            message.append("(2) The transaction ID is invalid or corrupted (Current ID: '")
                   .append(payment.getTransactionId()).append("'), ");
            message.append("(3) The payment is too old and no longer available for refund. ");
            message.append("Please contact support for manual refund processing.");
        } else if (errorDetails.contains("TRANSACTION_REFUSED") || errorMessage.contains("TRANSACTION_REFUSED")) {
            message.append("The refund was refused by PayPal. ");
            message.append("The transaction may have already been refunded or is not eligible for refund. ");
            message.append("Please contact support.");
        } else if (errorDetails.contains("INSUFFICIENT_FUNDS") || errorMessage.contains("INSUFFICIENT_FUNDS")) {
            message.append("Insufficient funds in merchant account to process refund. ");
            message.append("Please contact support immediately.");
        } else {
            // Generic error
            message.append("PayPal returned an error: ").append(errorMessage).append(". ");
            message.append("Please contact support with this booking ID for assistance.");
        }

        // Always include booking ID and payment ID for support
        message.append(" [Booking ID: ").append(bookingId)
               .append(", Payment ID: ").append(payment.getId())
               .append(", Transaction ID: ").append(payment.getTransactionId()).append("]");

        return message.toString();
    }

    @Override
    @Transactional
    public Booking processPayment(UUID bookingId) {
        log.info("Processing payment for booking {}", bookingId);

        // 1. Find booking with all relationships
        Booking booking = bookingRepository.findWithDetailById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found with id: " + bookingId));

        // 2. Validate booking status
        if (!booking.getStatus().equals(Constants.BookingStatusEnum.CANCELED) && 
            !booking.getStatus().equals(Constants.BookingStatusEnum.COMPLETED)) {
            throw new IllegalArgumentException(
                "Booking must be CANCELED or COMPLETED to process payment. Current status: " + booking.getStatus()
            );
        }

        // 3. Find completed payment
        BookingPayment completedPayment = booking.getBookingPayments().stream()
                .filter(p -> p.getStatus().equals(Constants.PaymentStatusEnum.COMPLETED))
                .findFirst()
                .orElse(null);

        if (completedPayment == null) {
            throw new IllegalArgumentException("No completed payment found for this booking. Cannot process payment.");
        }

        // 4. Process based on booking status
        if (booking.getStatus().equals(Constants.BookingStatusEnum.CANCELED)) {
            // REFUND: Refund to customer's PayPal account
            log.info("Booking {} is CANCELED, processing refund", bookingId);

            // Check if already refunded
            boolean hasRefundedPayment = booking.getBookingPayments().stream()
                    .anyMatch(p -> p.getStatus().equals(Constants.PaymentStatusEnum.REFUNDED));

            if (hasRefundedPayment) {
                throw new IllegalArgumentException("This booking has already been refunded");
            }

            try {
                PaymentStrategy paymentStrategy = paymentStrategies.get(completedPayment.getPaymentMethod());
                if (paymentStrategy == null) {
                    throw new IllegalArgumentException(
                        "Payment method not supported for refund: " + completedPayment.getPaymentMethod()
                    );
                }

                // Process refund
                BookingPayment refundedPayment = paymentStrategy.refund(bookingId, completedPayment);
                log.info("Successfully refunded payment {} for canceled booking {}", refundedPayment.getId(), bookingId);

                return bookingRepository.findWithDetailById(bookingId)
                        .orElseThrow(() -> new NotFoundException("Booking not found with id: " + bookingId));

            } catch (PayPalRESTException e) {
                log.error("PayPal refund failed for booking {}: {} - Details: {}",
                        bookingId, e.getMessage(), e.getDetails(), e);
                String errorMessage = buildRefundErrorMessage(bookingId, completedPayment, e);
                throw new RuntimeException(errorMessage, e);
            } catch (Exception e) {
                log.error("Unexpected error during refund for booking {}: {}", bookingId, e.getMessage(), e);
                throw new RuntimeException("Failed to process refund. Please contact support with booking ID: " + bookingId, e);
            }

        } else if (booking.getStatus().equals(Constants.BookingStatusEnum.COMPLETED)) {
            // PAYOUT: Transfer money to seer's PayPal email
            log.info("Booking {} is COMPLETED, processing payout to seer", bookingId);

            // Get seer and seer profile
            var seer = booking.getServicePackage().getSeer();
            if (seer == null || seer.getSeerProfile() == null) {
                throw new IllegalArgumentException("Seer or seer profile not found for this booking");
            }

            String paypalEmail = seer.getSeerProfile().getPaypalEmail();
            if (paypalEmail == null || paypalEmail.trim().isEmpty()) {
                throw new IllegalArgumentException(
                    "Seer has not set up PayPal email. Please set PayPal email before processing payout."
                );
            }

            // Calculate amount to payout (price - service_fee_amount = seer receives)
            Double bookingPrice = booking.getServicePackage().getPrice();
            Double serviceFeeAmount = booking.getServicePackage().getServiceFeeAmount();
            if (serviceFeeAmount == null) {
                // Fallback: calculate from commission rate if service_fee_amount is null
                Double commissionRate = booking.getServicePackage().getCommissionRate();
                if (commissionRate == null) {
                    commissionRate = 0.10; // Default 10%
                }
                serviceFeeAmount = bookingPrice * commissionRate;
            }
            Double seerAmount = bookingPrice - serviceFeeAmount;

            // Convert to USD (VND to USD conversion rate)
            double VND_TO_USD = 0.0395;
            double seerAmountInUSD = seerAmount * VND_TO_USD;

            try {
                // Process payout via PayPal
                Map<String, Object> payoutResponse = payPalGateway.payoutToSeer(
                        paypalEmail,
                        seerAmountInUSD,
                        bookingId.toString()
                );

                // Extract batch header info from response
                Map<String, Object> batchHeader = payoutResponse != null ?
                    (Map<String, Object>) payoutResponse.get("batch_header") : null;

                String batchId = batchHeader != null ? (String) batchHeader.get("payout_batch_id") : "N/A";
                String batchStatus = batchHeader != null ? (String) batchHeader.get("batch_status") : "N/A";

                log.info("Successfully processed payout for booking {} to seer {} (${}). " +
                        "Batch ID: {}, Status: {}",
                        bookingId, paypalEmail, seerAmountInUSD, batchId, batchStatus);

                // Create new payment record for SEER payout (RECEIVED_PACKAGE)
                BookingPayment seerPayoutPayment = BookingPayment.builder()
                        .booking(booking)
                        .amount(seerAmount)  // Amount in VND that seer receives
                        .status(Constants.PaymentStatusEnum.COMPLETED)
                        .paymentMethod(Constants.PaymentMethodEnum.PAYPAL)
                        .paymentType(Constants.PaymentTypeEnum.RECEIVED_PACKAGE)
                        .transactionId(batchId)
                        .extraInfo(String.format("Payout Batch ID: %s, Status: %s, Amount: %.2f USD, Seer PayPal: %s, Service Fee: %.2f VND",
                                batchId, batchStatus, seerAmountInUSD, paypalEmail, serviceFeeAmount))
                        .build();

                bookingPaymentRepository.save(seerPayoutPayment);

                log.info("Created RECEIVED_PACKAGE payment record for seer. Payment ID: {}, Amount: {} VND",
                        seerPayoutPayment.getId(), seerAmount);

                return bookingRepository.findWithDetailById(bookingId)
                        .orElseThrow(() -> new NotFoundException("Booking not found with id: " + bookingId));

            } catch (Exception e) {
                log.error("Unexpected error during payout for booking {}: {}", bookingId, e.getMessage(), e);
                throw new RuntimeException("Failed to process payout. Please contact support with booking ID: " + bookingId, e);
            }
        }

        throw new IllegalStateException("Unexpected booking status: " + booking.getStatus());
    }

    @Override
    @Transactional(readOnly = true)
    public DailyRevenueResponse getDailyRevenue(LocalDate date) {
        LocalDateTime dateTime = date.atStartOfDay();

        // Lấy tất cả payments COMPLETED trong ngày
        List<BookingPayment> payments = bookingPaymentRepository.findPaymentsByStatusAndTypesAndDate(
                Constants.PaymentStatusEnum.COMPLETED,
                Arrays.asList(
                    Constants.PaymentTypeEnum.PAID_PACKAGE,
                    Constants.PaymentTypeEnum.RECEIVED_PACKAGE
                ),
                dateTime
        );

        // Tính doanh thu: PAID_PACKAGE (khách thanh toán) - RECEIVED_PACKAGE (trả lại cho seer)
        double totalRevenue = 0.0;

        for (BookingPayment payment : payments) {
            if (payment.getPaymentType() == Constants.PaymentTypeEnum.PAID_PACKAGE) {
                // Cộng tiền khách hàng thanh toán
                totalRevenue += payment.getAmount();
            } else if (payment.getPaymentType() == Constants.PaymentTypeEnum.RECEIVED_PACKAGE) {
                // Trừ tiền đã trả cho seer
                totalRevenue -= payment.getAmount();
            }
        }

        // Thuế cố định 10%
        double taxPercentage = 10.0;

        return DailyRevenueResponse.builder()
                .date(date)
                .totalRevenue(totalRevenue)
                .taxPercentage(taxPercentage)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingPayment> getMySeerSalary(
            Constants.PaymentTypeEnum paymentType,
            Constants.PaymentStatusEnum paymentStatus,
            Integer year,
            Integer month,
            Integer day,
            Pageable pageable
    ) {
        User currentSeer = userService.getUser();

        // Validate paymentType: chỉ cho phép RECEIVED_PACKAGE hoặc BONUS
        if (paymentType != null &&
            paymentType != Constants.PaymentTypeEnum.RECEIVED_PACKAGE &&
            paymentType != Constants.PaymentTypeEnum.BONUS) {
            throw new IllegalArgumentException("Invalid payment type. Only RECEIVED_PACKAGE and BONUS are allowed for salary queries.");
        }

        // Fetch large dataset to filter by date
        final int MAX_FETCH = 10000;
        Pageable fetchPageable = org.springframework.data.domain.PageRequest.of(0, MAX_FETCH, pageable.getSort());

        // Get all salary payments for current seer (RECEIVED_PACKAGE from bookings + BONUS)
        List<BookingPayment> allPayments = new java.util.ArrayList<>();

        // 1. Get RECEIVED_PACKAGE payments (from bookings where seer owns the package)
        Page<BookingPayment> receivedPackagePayments = bookingPaymentRepository.findAll(
            (root, query, cb) -> {
                // Ensure fetch joins so related entities are initialized within the same session
                root.fetch("booking", JoinType.LEFT).fetch("customer", JoinType.LEFT);
                root.fetch("booking", JoinType.LEFT).fetch("servicePackage", JoinType.LEFT).fetch("seer", JoinType.LEFT);
                query.distinct(true);

                List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();

                // Filter by payment type RECEIVED_PACKAGE
                predicates.add(cb.equal(root.get("paymentType"), Constants.PaymentTypeEnum.RECEIVED_PACKAGE));

                // Filter by seer through booking.servicePackage.seer
                predicates.add(cb.equal(root.get("booking").get("servicePackage").get("seer").get("id"), currentSeer.getId()));

                // Filter by payment status if provided
                if (paymentStatus != null) {
                    predicates.add(cb.equal(root.get("status"), paymentStatus));
                }

                return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
            },
            fetchPageable
        );

        // Add RECEIVED_PACKAGE payments if not filtering by BONUS only
        if (paymentType == null || paymentType == Constants.PaymentTypeEnum.RECEIVED_PACKAGE) {
            allPayments.addAll(receivedPackagePayments.getContent());
        }

        // 2. Get BONUS payments (direct seer relation)
        if (paymentType == null || paymentType == Constants.PaymentTypeEnum.BONUS) {
            Page<BookingPayment> bonusPayments = bookingPaymentRepository.findAll(
                (root, query, cb) -> {
                    // Fetch seer relation on payment and ensure booking is fetched in case mapping needs it
                    root.fetch("seer", JoinType.LEFT);
                    // also fetch booking and nested relations to be safe
                    root.fetch("booking", JoinType.LEFT).fetch("customer", JoinType.LEFT);
                    root.fetch("booking", JoinType.LEFT).fetch("servicePackage", JoinType.LEFT).fetch("seer", JoinType.LEFT);
                    query.distinct(true);

                    List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();

                    // Filter by payment type BONUS
                    predicates.add(cb.equal(root.get("paymentType"), Constants.PaymentTypeEnum.BONUS));

                    // Filter by seer directly
                    predicates.add(cb.equal(root.get("seer").get("id"), currentSeer.getId()));

                    // Filter by payment status if provided
                    if (paymentStatus != null) {
                        predicates.add(cb.equal(root.get("status"), paymentStatus));
                    }

                    return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
                },
                fetchPageable
            );
            allPayments.addAll(bonusPayments.getContent());
        }

        // Filter by date
        List<BookingPayment> filtered = filterPaymentsByDate(allPayments, year, month, day);

        // Manual pagination
        return paginatePayments(filtered, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingPayment> getAllSeerSalary(
            Constants.PaymentTypeEnum paymentType,
            Constants.PaymentStatusEnum paymentStatus,
            Integer year,
            Integer month,
            Integer day,
            Pageable pageable
    ) {
        // Validate paymentType: chỉ cho phép RECEIVED_PACKAGE hoặc BONUS
        if (paymentType != null &&
            paymentType != Constants.PaymentTypeEnum.RECEIVED_PACKAGE &&
            paymentType != Constants.PaymentTypeEnum.BONUS) {
            throw new IllegalArgumentException("Invalid payment type. Only RECEIVED_PACKAGE and BONUS are allowed for salary queries.");
        }

        // Fetch large dataset to filter by date
        final int MAX_FETCH = 10000;
        Pageable fetchPageable = org.springframework.data.domain.PageRequest.of(0, MAX_FETCH, pageable.getSort());

        List<BookingPayment> allPayments = new java.util.ArrayList<>();

        // 1. Get all RECEIVED_PACKAGE payments
        if (paymentType == null || paymentType == Constants.PaymentTypeEnum.RECEIVED_PACKAGE) {
            Page<BookingPayment> receivedPackagePayments = bookingPaymentRepository.findAll(
                (root, query, cb) -> {
                    // Fetch booking and nested seer/customer
                    root.fetch("booking", JoinType.LEFT).fetch("customer", JoinType.LEFT);
                    root.fetch("booking", JoinType.LEFT).fetch("servicePackage", JoinType.LEFT).fetch("seer", JoinType.LEFT);
                    query.distinct(true);

                    List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();

                    // Filter by payment type RECEIVED_PACKAGE
                    predicates.add(cb.equal(root.get("paymentType"), Constants.PaymentTypeEnum.RECEIVED_PACKAGE));

                    // Filter by payment status if provided
                    if (paymentStatus != null) {
                        predicates.add(cb.equal(root.get("status"), paymentStatus));
                    }

                    return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
                },
                fetchPageable
            );
            allPayments.addAll(receivedPackagePayments.getContent());
        }

        // 2. Get all BONUS payments
        if (paymentType == null || paymentType == Constants.PaymentTypeEnum.BONUS) {
            Page<BookingPayment> bonusPayments = bookingPaymentRepository.findAll(
                (root, query, cb) -> {
                    // Fetch seer relation and booking nested relations
                    root.fetch("seer", JoinType.LEFT);
                    root.fetch("booking", JoinType.LEFT).fetch("customer", JoinType.LEFT);
                    root.fetch("booking", JoinType.LEFT).fetch("servicePackage", JoinType.LEFT).fetch("seer", JoinType.LEFT);
                    query.distinct(true);

                    List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();

                    // Filter by payment type BONUS
                    predicates.add(cb.equal(root.get("paymentType"), Constants.PaymentTypeEnum.BONUS));

                    // Filter by payment status if provided
                    if (paymentStatus != null) {
                        predicates.add(cb.equal(root.get("status"), paymentStatus));
                    }

                    return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
                },
                fetchPageable
            );
            allPayments.addAll(bonusPayments.getContent());
        }

        // Filter by date
        List<BookingPayment> filtered = filterPaymentsByDate(allPayments, year, month, day);

        // Manual pagination
        return paginatePayments(filtered, pageable);
    }

    // Helper method to filter payments by date
    private List<BookingPayment> filterPaymentsByDate(List<BookingPayment> payments, Integer year, Integer month, Integer day) {
        if (year == null && month == null && day == null) {
            return payments; // No date filter
        }

        java.time.LocalDate targetDate = null;
        if (year != null && month != null && day != null) {
            try {
                targetDate = java.time.LocalDate.of(year, month, day);
            } catch (Exception ex) {
                throw new IllegalArgumentException("Invalid date parameters: " + ex.getMessage());
            }
        }

        List<BookingPayment> filtered = new java.util.ArrayList<>();
        for (BookingPayment payment : payments) {
            java.time.LocalDateTime createdAt = payment.getCreatedAt();
            if (createdAt == null) continue;

            java.time.LocalDate paymentDate = createdAt.toLocalDate();
            boolean match = false;

            if (targetDate != null) {
                match = paymentDate.equals(targetDate);
            } else if (year != null && month != null) {
                match = (paymentDate.getYear() == year && paymentDate.getMonthValue() == month);
            } else if (year != null) {
                match = (paymentDate.getYear() == year);
            }

            if (match) filtered.add(payment);
        }

        return filtered;
    }

    // Helper method to paginate payments manually
    private Page<BookingPayment> paginatePayments(List<BookingPayment> payments, Pageable pageable) {
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, payments.size());

        List<BookingPayment> pageContent;
        if (fromIndex >= payments.size()) {
            pageContent = java.util.Collections.emptyList();
        } else {
            pageContent = payments.subList(fromIndex, toIndex);
        }

        return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, payments.size());
    }

    @Override
    @Transactional(readOnly = true)
    public com.iseeyou.fortunetelling.dto.response.booking.BookingPaymentStatsResponse getPaymentStats() {
        // 1. Lấy tất cả các payment PAID_PACKAGE với status COMPLETED
        List<BookingPayment> paidPackagePayments = bookingPaymentRepository.findAllByPaymentTypeAndStatus(
                Constants.PaymentTypeEnum.PAID_PACKAGE,
                Constants.PaymentStatusEnum.COMPLETED
        );

        // 2. Lấy tất cả các payment RECEIVED_PACKAGE với status COMPLETED
        List<BookingPayment> receivedPackagePayments = bookingPaymentRepository.findAllByPaymentTypeAndStatus(
                Constants.PaymentTypeEnum.RECEIVED_PACKAGE,
                Constants.PaymentStatusEnum.COMPLETED
        );

        // 3. Lấy tất cả các payment REFUNDED
        List<BookingPayment> refundedPayments = bookingPaymentRepository.findAllByStatusList(
                Constants.PaymentStatusEnum.REFUNDED
        );

        // 4. Tính tổng tiền khách hàng đã thanh toán
        double totalPaidAmount = paidPackagePayments.stream()
                .mapToDouble(BookingPayment::getAmount)
                .sum();

        // 5. Tính tổng tiền đã trả cho seer
        double totalReceivedAmount = receivedPackagePayments.stream()
                .mapToDouble(BookingPayment::getAmount)
                .sum();

        // 6. Tính doanh thu (phí dịch vụ = tiền khách trả - tiền trả cho seer)
        double totalRevenue = totalPaidAmount - totalReceivedAmount;

        // 7. Đếm số booking thành công (mỗi booking chỉ tính 1 lần)
        // Lấy số booking unique từ PAID_PACKAGE COMPLETED
        Long successfulTransactions = bookingPaymentRepository.countDistinctBookingsByPaymentTypeAndStatus(
                Constants.PaymentTypeEnum.PAID_PACKAGE,
                Constants.PaymentStatusEnum.COMPLETED
        );

        // 8. Tính số giao dịch bị refund và tổng tiền refund
        // Đếm số booking unique bị refund
        long refundedTransactions = refundedPayments.stream()
                .filter(p -> p.getBooking() != null)
                .map(p -> p.getBooking().getId())
                .distinct()
                .count();

        // Tổng tiền đã hoàn lại
        double totalRefundedAmount = refundedPayments.stream()
                .mapToDouble(BookingPayment::getAmount)
                .sum();

        // 9. Tạo response với 4 trường theo yêu cầu
        return com.iseeyou.fortunetelling.dto.response.booking.BookingPaymentStatsResponse.builder()
                .totalRevenue(totalRevenue)
                .successfulTransactions(successfulTransactions)
                .refundedTransactions(refundedTransactions)
                .totalRefundedAmount(totalRefundedAmount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingPayment> findPaymentsByUserId(UUID userId, Pageable pageable) {
        log.info("Finding payments by userId: {}", userId);
        // Trả về các payment mà user đã thanh toán (type PAID_PACKAGE)
        return bookingPaymentRepository.findAllByUserId(userId, Constants.PaymentTypeEnum.PAID_PACKAGE, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingPayment> findPaymentsBySeerId(UUID seerId, Pageable pageable) {
        log.info("Finding payments by seerId: {}", seerId);
        // Trả về các payment mà seer đã nhận (type RECEIVED_PACKAGE và BONUS)
        return bookingPaymentRepository.findAllBySeerId(
                seerId,
                Constants.PaymentTypeEnum.RECEIVED_PACKAGE,
                Constants.PaymentTypeEnum.BONUS,
                pageable
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingPayment> findPaymentsByRole(Constants.RoleEnum role, Pageable pageable) {
        log.info("Finding payments by role: {}", role);
        
        if (role == Constants.RoleEnum.CUSTOMER || role == Constants.RoleEnum.GUEST) {
            // Trả về tất cả payment USER đã thực hiện (type PAID_PACKAGE)
            return bookingPaymentRepository.findAllByRoleUser(Constants.PaymentTypeEnum.PAID_PACKAGE, pageable);
        } else if (role == Constants.RoleEnum.SEER || role == Constants.RoleEnum.UNVERIFIED_SEER) {
            // Trả về tất cả payment đã thực hiện tới SEER (type RECEIVED_PACKAGE và BONUS)
            return bookingPaymentRepository.findAllByRoleSeer(
                    Arrays.asList(
                            Constants.PaymentTypeEnum.RECEIVED_PACKAGE,
                            Constants.PaymentTypeEnum.BONUS
                    ),
                    pageable
            );
        } else {
            throw new IllegalArgumentException("Invalid role for payment filtering. Only USER, CUSTOMER, GUEST, SEER, or UNVERIFIED_SEER are supported.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingPayment> findPaymentsByUserOrSeerName(String searchName, Pageable pageable) {
        log.info("Finding payments by user/seer name: {}", searchName);

        if (searchName == null || searchName.trim().isEmpty()) {
            throw new IllegalArgumentException("Search name cannot be empty");
        }

        // Tìm kiếm theo tên user (customer) hoặc seer
        return bookingPaymentRepository.findAllByUserOrSeerName(searchName.trim(), pageable);
    }
}
