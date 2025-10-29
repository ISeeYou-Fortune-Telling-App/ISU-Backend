package com.iseeyou.fortunetelling.service.booking.strategy;

import com.iseeyou.fortunetelling.entity.booking.Booking;
import com.iseeyou.fortunetelling.entity.booking.BookingPayment;
import com.paypal.base.rest.PayPalRESTException;

import java.util.Map;
import java.util.UUID;

public interface PaymentStrategy {
    BookingPayment pay(Booking booking) throws PayPalRESTException;
    BookingPayment executePayment(Map<String, Object> paymentParams);
    BookingPayment refund(UUID bookingId, BookingPayment payment) throws PayPalRESTException;
}
