package com.iseeyou.fortunetelling.service.booking.strategy.impl;

import com.iseeyou.fortunetelling.entity.booking.Booking;
import com.iseeyou.fortunetelling.entity.booking.BookingPayment;
import com.iseeyou.fortunetelling.repository.booking.BookingPaymentRepository;
import com.iseeyou.fortunetelling.service.booking.strategy.PaymentStrategy;
import com.iseeyou.fortunetelling.service.booking.strategy.gateway.VNPayGateway;
import com.iseeyou.fortunetelling.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class VNPayStrategy implements PaymentStrategy {

    private final VNPayGateway vnPayGateway;
    private final BookingPaymentRepository bookingPaymentRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BookingPayment pay(Booking booking, String successUrl, String cancelUrl) {
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BookingPayment executePayment(Map<String, Object> paymentParams) {
        return null;
    }
}
