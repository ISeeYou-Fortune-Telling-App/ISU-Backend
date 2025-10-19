package com.iseeyou.fortunetelling.service.booking;

import com.iseeyou.fortunetelling.entity.booking.Booking;
import com.iseeyou.fortunetelling.entity.booking.BookingPayment;
import com.iseeyou.fortunetelling.entity.booking.BookingReview;
import com.iseeyou.fortunetelling.util.Constants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.UUID;

public interface BookingService {
    Page<Booking> getBookingsByMe(Pageable pageable);
    Page<Booking> getBookingsByMeAndStatus(Constants.BookingStatusEnum status, Pageable pageable);
    
    Booking findById(UUID id);
    Page<BookingReview> findAllReviewByBookingId(UUID id, Pageable pageable);
    Page<BookingPayment> findAllByPaymentMethod(Constants.PaymentMethodEnum paymentMethodEnum, Pageable pageable);
    Page<BookingPayment> findAllByStatus(Constants.PaymentStatusEnum statusEnum, Pageable pageable);
    Page<BookingPayment> findAllBookingPayments(Pageable pageable);
    BookingPayment findPaymentById(UUID id);
    Booking createBooking(Booking booking, UUID packageId, Constants.PaymentMethodEnum paymentMethod);
    BookingPayment executePayment(Constants.PaymentMethodEnum paymentMethod, Map<String, Object> paymentParams);
    Booking updateBooking(Booking booking);
    void deleteBooking(UUID id);
    Booking refundBooking(UUID id);
}
