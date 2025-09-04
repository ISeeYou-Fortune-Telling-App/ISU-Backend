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
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class VNPayStrategy implements PaymentStrategy {

    private final VNPayGateway vnPayGateway;
    private final BookingPaymentRepository bookingPaymentRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BookingPayment pay(Booking booking) {
        Double amount = booking.getServicePackage().getPrice();
        try {
            String vnpayUrl = vnPayGateway.createPaymentUrl(booking.getId().toString(), amount.longValue() * 1000L, "1.1.1.1");
            BookingPayment bookingPayment = new BookingPayment();
            bookingPayment.setBooking(booking);
            bookingPayment.setAmount(amount);
            bookingPayment.setStatus(Constants.PaymentStatusEnum.PENDING);
            bookingPayment.setPaymentMethod(Constants.PaymentMethodEnum.VNPAY);
            bookingPayment.setApprovalUrl(vnpayUrl);

            return bookingPaymentRepository.save(bookingPayment);
        } catch (Exception e) {
            log.error("VNPay payment creation failed: {}", e.getMessage());

            BookingPayment bookingPayment = new BookingPayment();
            bookingPayment.setBooking(booking);
            bookingPayment.setAmount(amount);
            bookingPayment.setStatus(Constants.PaymentStatusEnum.FAILED);
            bookingPayment.setPaymentMethod(Constants.PaymentMethodEnum.VNPAY);
            bookingPayment.setFailureReason(e.getMessage());

            return bookingPaymentRepository.save(bookingPayment);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BookingPayment executePayment(Map<String, Object> paymentParams) {
        String vnp_BankCode = (String) paymentParams.get("vnp_BankCode");
        String vnp_CardType = (String) paymentParams.get("vnp_CardType");
        String vnp_TransactionNo = (String) paymentParams.get("vnp_TransactionNo");
        String vnp_ResponseCode = (String) paymentParams.get("vnp_ResponseCode");
        String vnp_TxnRef = (String) paymentParams.get("vnp_TxnRef");

        String bookingId = vnp_TxnRef.split("_")[0];
        BookingPayment currentBookingPayment = bookingPaymentRepository.findByBooking_Id(UUID.fromString(bookingId));
        String extraInfo = vnp_BankCode + vnp_CardType;
        currentBookingPayment.setExtraInfo(extraInfo);
        currentBookingPayment.setTransactionId(vnp_TransactionNo);

        if (vnp_ResponseCode.equals("00")) {
            currentBookingPayment.setStatus(Constants.PaymentStatusEnum.COMPLETED);
            // Additional logic for successful payment can be added here
        } else {
            currentBookingPayment.setStatus(Constants.PaymentStatusEnum.FAILED);
            currentBookingPayment.setFailureReason("VNPay payment failed with response code: " + vnp_ResponseCode);
            // Additional logic for failed payment can be added here
        }

        return bookingPaymentRepository.save(currentBookingPayment);
    }
}