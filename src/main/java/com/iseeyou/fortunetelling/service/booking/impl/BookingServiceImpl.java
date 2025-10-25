package com.iseeyou.fortunetelling.service.booking.impl;

import com.iseeyou.fortunetelling.dto.request.booking.BookingCreateRequest;
import com.iseeyou.fortunetelling.dto.request.booking.BookingReviewRequest;
import com.iseeyou.fortunetelling.dto.request.booking.BookingUpdateRequest;
import com.iseeyou.fortunetelling.dto.response.booking.BookingReviewResponse;
import com.iseeyou.fortunetelling.entity.booking.Booking;
import com.iseeyou.fortunetelling.entity.booking.BookingPayment;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.exception.NotFoundException;
import com.iseeyou.fortunetelling.mapper.BookingMapper;
import com.iseeyou.fortunetelling.repository.booking.BookingPaymentRepository;
import com.iseeyou.fortunetelling.repository.booking.BookingRepository;
import com.iseeyou.fortunetelling.service.booking.BookingService;
import com.iseeyou.fortunetelling.service.booking.strategy.PaymentStrategy;
import com.iseeyou.fortunetelling.service.converstation.ConverstationService;
import com.iseeyou.fortunetelling.service.servicepackage.ServicePackageService;
import com.iseeyou.fortunetelling.service.user.UserService;
import com.iseeyou.fortunetelling.util.Constants;
import com.paypal.base.rest.PayPalRESTException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BookingPaymentRepository bookingPaymentRepository;
    private final UserService userService;
    private final ServicePackageService servicePackageService;
    private final ConverstationService conversationService;
    private final BookingMapper bookingMapper;
    private final Map<Constants.PaymentMethodEnum, PaymentStrategy> paymentStrategies;

    @Override
    @Transactional(readOnly = true)
    public Page<Booking> getBookingsByMe(Pageable pageable) {
        User currentUser = userService.getUser();
        if (currentUser.getRole().equals(Constants.RoleEnum.CUSTOMER)) {
            return bookingRepository.findAllByCustomer(currentUser, pageable);
        } else if (currentUser.getRole().equals(Constants.RoleEnum.SEER)) {
            return bookingRepository.findAllBySeer(currentUser, pageable);
        } else {
            return bookingRepository.findAll(pageable);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Booking> getBookingsByMeAndStatus(Constants.BookingStatusEnum status, Pageable pageable) {
        User currentUser = userService.getUser();
        if (currentUser.getRole().equals(Constants.RoleEnum.CUSTOMER)) {
            return bookingRepository.findAllByCustomerAndStatus(currentUser, status, pageable);
        } else if (currentUser.getRole().equals(Constants.RoleEnum.SEER)) {
            return bookingRepository.findAllBySeerAndStatus(currentUser, status, pageable);
        } else {
            return bookingRepository.findAllByStatus(status, pageable);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Booking findById(UUID id) {
        return bookingRepository.findById(id)
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

        Booking newBooking = bookingRepository.save(booking);

        try {
            BookingPayment bookingPayment = createBookingPayment(newBooking, request.getPaymentMethod());
            newBooking.getBookingPayments().add(bookingPayment);
        } catch (PayPalRESTException e) {
            log.error("Error creating PayPal payment: {}", e.getMessage());
            newBooking.setStatus(Constants.BookingStatusEnum.FAILED);

            throw new RuntimeException("Error creating payment", e);
        }

        return bookingRepository.save(newBooking);
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

        Booking updatedBooking = bookingRepository.save(existingBooking);

        // create chat session if booking confirmed
        if (statusChangedToConfirmed) {
            log.info("Booking confirmed, creating chat session for booking: {}", id);
            conversationService.createChatSession(id);
        }

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
    public Booking refundBooking(UUID id) {
        return null;
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
}
