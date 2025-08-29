package com.iseeyou.fortunetelling.service.booking.strategy;

import com.iseeyou.fortunetelling.entity.booking.Booking;
import com.iseeyou.fortunetelling.entity.booking.BookingPayment;
import com.paypal.base.rest.PayPalRESTException;

import java.util.Map;

public interface PaymentStrategy {
    BookingPayment pay(Booking booking, String successUrl, String cancelUrl) throws PayPalRESTException;
    BookingPayment executePayment(Map<String, Object> paymentParams);
}
