package com.iseeyou.fortunetelling.service.booking.strategy.impl;

import com.iseeyou.fortunetelling.entity.booking.Booking;
import com.iseeyou.fortunetelling.entity.booking.BookingPayment;
import com.iseeyou.fortunetelling.repository.booking.BookingPaymentRepository;
import com.iseeyou.fortunetelling.service.booking.strategy.PaymentStrategy;
import com.iseeyou.fortunetelling.service.booking.strategy.gateway.VNPayGateway;
import com.iseeyou.fortunetelling.util.Constants;
import com.paypal.base.rest.PayPalRESTException;
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
        log.info("Executing VNPay payment with params: {}", paymentParams);
        
        String vnp_BankCode = (String) paymentParams.get("vnp_BankCode");
        String vnp_CardType = (String) paymentParams.get("vnp_CardType");
        String vnp_TransactionNo = (String) paymentParams.get("vnp_TransactionNo");
        String vnp_ResponseCode = (String) paymentParams.get("vnp_ResponseCode");
        String vnp_TxnRef = (String) paymentParams.get("vnp_TxnRef");

        // Validate required parameters
        if (vnp_TxnRef == null || vnp_TxnRef.trim().isEmpty()) {
            log.error("Missing required parameter: vnp_TxnRef");
            throw new IllegalArgumentException("Missing required VNPay transaction reference");
        }
        
        if (vnp_ResponseCode == null || vnp_ResponseCode.trim().isEmpty()) {
            log.error("Missing required parameter: vnp_ResponseCode");
            throw new IllegalArgumentException("Missing required VNPay response code");
        }

        // Extract booking ID from transaction reference
        String bookingId;
        try {
            bookingId = vnp_TxnRef.split("_")[0];
            log.debug("Extracted booking ID: {} from vnp_TxnRef: {}", bookingId, vnp_TxnRef);
        } catch (Exception e) {
            log.error("Failed to extract booking ID from vnp_TxnRef: {}", vnp_TxnRef);
            throw new IllegalArgumentException("Invalid VNPay transaction reference format: " + vnp_TxnRef);
        }
        
        // Find payment by booking ID
        BookingPayment currentBookingPayment;
        try {
            currentBookingPayment = bookingPaymentRepository.findByBooking_Id(UUID.fromString(bookingId));
            if (currentBookingPayment == null) {
                log.error("No payment found for booking ID: {}", bookingId);
                throw new IllegalArgumentException("Payment not found for booking: " + bookingId);
            }
        } catch (IllegalArgumentException e) {
            log.error("Invalid booking ID format: {}", bookingId);
            throw new IllegalArgumentException("Invalid booking ID format: " + bookingId, e);
        }
        String extraInfo = vnp_BankCode + vnp_CardType;
        currentBookingPayment.setExtraInfo(extraInfo);
        currentBookingPayment.setTransactionId(vnp_TransactionNo);
        
        log.info("Processing VNPay payment for booking {}, response code: {}", bookingId, vnp_ResponseCode);

        if (vnp_ResponseCode.equals("00")) {
            currentBookingPayment.setStatus(Constants.PaymentStatusEnum.COMPLETED);
            log.info("VNPay payment completed successfully for booking {}, transaction: {}", bookingId, vnp_TransactionNo);
            // Additional logic for successful payment can be added here
        } else {
            currentBookingPayment.setStatus(Constants.PaymentStatusEnum.FAILED);
            currentBookingPayment.setFailureReason("VNPay payment failed with response code: " + vnp_ResponseCode);
            log.warn("VNPay payment failed for booking {}, response code: {}", bookingId, vnp_ResponseCode);
            // Additional logic for failed payment can be added here
        }

        return bookingPaymentRepository.save(currentBookingPayment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BookingPayment refund(UUID bookingId, BookingPayment payment) throws PayPalRESTException {
        log.warn("VNPay refund is not yet implemented for booking {} with payment {}", bookingId, payment.getId());
        throw new UnsupportedOperationException("VNPay refund functionality is not yet implemented. Please contact support for manual refund.");
    }
}