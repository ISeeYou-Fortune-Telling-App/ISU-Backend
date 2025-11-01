package com.iseeyou.fortunetelling.service.booking;

import com.iseeyou.fortunetelling.dto.request.booking.BookingCreateRequest;
import com.iseeyou.fortunetelling.dto.request.booking.BookingReviewRequest;
import com.iseeyou.fortunetelling.dto.request.booking.BookingUpdateRequest;
import com.iseeyou.fortunetelling.dto.response.booking.BookingReviewResponse;
import com.iseeyou.fortunetelling.entity.booking.Booking;
import com.iseeyou.fortunetelling.entity.booking.BookingPayment;
import com.iseeyou.fortunetelling.util.Constants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.UUID;

public interface BookingService {
    Page<Booking> getBookingsByMe(Pageable pageable);
    Page<Booking> getBookingsByMeAndStatus(Constants.BookingStatusEnum status, Pageable pageable);
    
    // Admin methods to get all bookings
    Page<Booking> getAllBookings(Pageable pageable);
    Page<Booking> getAllBookingsByStatus(Constants.BookingStatusEnum status, Pageable pageable);

    Booking findById(UUID id);
    Page<BookingPayment> findAllByPaymentMethod(Constants.PaymentMethodEnum paymentMethodEnum, Pageable pageable);
    Page<BookingPayment> findAllByStatus(Constants.PaymentStatusEnum statusEnum, Pageable pageable);
    Page<BookingPayment> findAllBookingPayments(Pageable pageable);
    BookingPayment findPaymentById(UUID id);
    Booking createBooking(BookingCreateRequest request, UUID packageId);
    BookingPayment executePayment(Constants.PaymentMethodEnum paymentMethod, Map<String, Object> paymentParams);
    Booking updateBooking(UUID id, BookingUpdateRequest request);
    void deleteBooking(UUID id);
    Booking refundBooking(UUID id);
    Booking cancelBooking(UUID id);
    Booking seerConfirmBooking(UUID id, Constants.BookingStatusEnum status);

    // Review methods
    BookingReviewResponse submitReview(UUID bookingId, BookingReviewRequest reviewRequest);
    Page<BookingReviewResponse> getReviewsByServicePackage(UUID packageId, Pageable pageable);
    Page<BookingReviewResponse> adminGetReviews(UUID packageId, UUID seerId, Pageable pageable);
    Page<BookingReviewResponse> seerGetReviews(UUID packageId, Pageable pageable);

    // Admin/Debug methods
    Page<BookingPayment> findPaymentsWithInvalidTransactionIds(Pageable pageable);

    // New: Payments listing for seer and user
    Page<BookingPayment> seerGetPayments(UUID packageId, org.springframework.data.domain.Pageable pageable);
    Page<BookingPayment> userGetPayments(org.springframework.data.domain.Pageable pageable);
}
