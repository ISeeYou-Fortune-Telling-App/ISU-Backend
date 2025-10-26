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
    
    // Review methods
    BookingReviewResponse submitReview(UUID bookingId, BookingReviewRequest reviewRequest);
    Page<BookingReviewResponse> getReviewsByServicePackage(UUID packageId, Pageable pageable);
    
    // Admin/Debug methods
    Page<BookingPayment> findPaymentsWithInvalidTransactionIds(Pageable pageable);
}
