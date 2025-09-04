package com.iseeyou.fortunetelling.service.booking.strategy.impl;

import com.iseeyou.fortunetelling.entity.booking.Booking;
import com.iseeyou.fortunetelling.entity.booking.BookingPayment;
import com.iseeyou.fortunetelling.repository.booking.BookingPaymentRepository;
import com.iseeyou.fortunetelling.service.booking.strategy.PaymentStrategy;
import com.iseeyou.fortunetelling.service.booking.strategy.gateway.PayPalGateway;
import com.iseeyou.fortunetelling.util.Constants;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.RelatedResources;
import com.paypal.api.payments.Transaction;
import com.paypal.base.rest.PayPalRESTException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class PayPalStrategy implements PaymentStrategy {

    private final PayPalGateway payPalGateway;
    private final BookingPaymentRepository bookingPaymentRepository;

    // USD conversion rate - 1 VND = 0.0395 USD
    private static final double VND_TO_USD = 0.0395;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BookingPayment pay(Booking booking) throws PayPalRESTException {
        double amountInVND = booking.getServicePackage().getPrice();
        double amountInUSD = amountInVND * VND_TO_USD;
        try {
            Payment payment = payPalGateway.createPaymentWithPayPal(
                    amountInUSD,
                    String.valueOf(booking.getId())
            );

            // Get approval URL to redirect user
            String redirectUrl = payment.getLinks().stream()
                    .filter(link -> "approval_url".equals(link.getRel()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No approval URL found"))
                    .getHref();

            BookingPayment bookingPayment = new BookingPayment();
            bookingPayment.setBooking(booking);
            bookingPayment.setAmount(amountInVND);
            bookingPayment.setStatus(Constants.PaymentStatusEnum.PENDING);
            bookingPayment.setPaymentMethod(Constants.PaymentMethodEnum.PAYPAL);
            bookingPayment.setApprovalUrl(redirectUrl);
            bookingPayment.setExtraInfo(payment.getId());

            return bookingPaymentRepository.save(bookingPayment);

        } catch (PayPalRESTException e) {
            log.error("PayPal payment creation failed: {}", e.getMessage());

            BookingPayment bookingPayment = new BookingPayment();
            bookingPayment.setBooking(booking);
            bookingPayment.setAmount(amountInVND);
            bookingPayment.setStatus(Constants.PaymentStatusEnum.FAILED);
            bookingPayment.setPaymentMethod(Constants.PaymentMethodEnum.PAYPAL);
            bookingPayment.setFailureReason(e.getMessage());

            return bookingPaymentRepository.save(bookingPayment);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BookingPayment executePayment(Map<String, Object> paymentParams) {
        String paymentId = (String) paymentParams.get("paymentId");
        String payerId = (String) paymentParams.get("PayerID");

        BookingPayment bookingPayment = bookingPaymentRepository.findByExtraInfo(paymentId);

        try {

            if (bookingPayment == null) {
                log.error("BookingPayment not found for paymentId: {}", paymentId);
                return null;
            }

            Payment executedPayment = payPalGateway.executePayment(paymentId, payerId);

            String transactionId = null;
            List<Transaction> transactions = executedPayment.getTransactions();

            if (transactions != null && !transactions.isEmpty()) {
                Transaction transaction = transactions.get(0); // Lấy giao dịch đầu tiên
                List<RelatedResources> relatedResources = transaction.getRelatedResources();

                if (relatedResources != null && !relatedResources.isEmpty()) {
                    for (RelatedResources resource : relatedResources) {
                        if (resource.getSale() != null) {
                            transactionId = resource.getSale().getId(); // Lấy ID của sale
                            break;
                        }
                    }
                }
            }

            if (transactionId == null) {
                throw new PayPalRESTException("Không tìm thấy transactionId");
            }

            bookingPayment.setTransactionId(transactionId);

            // Update payment status based on PayPal response
            if ("approved".equals(executedPayment.getState())) {
                bookingPayment.setStatus(Constants.PaymentStatusEnum.COMPLETED);


                // TODO: send push notification here
//                if (orderPayment.getOrder().getUser() != null) {
//                    notificationService.createNotification(
//                            NotificationCreateRequestDTO.builder()
//                                    .notificationType(Constants.NotificationTypeEnum.PAYMENT)
//                                    .notificationContent("Thanh toán thành công cho đơn hàng " + orderPayment.getOrder().getId())
//                                    .senderId(null)
//                                    .receiverId(orderPayment.getOrder().getUser().getId())
//                                    .isRead(false)
//                                    .build()
//                    );
//                }
            } else {
                bookingPayment.setStatus(Constants.PaymentStatusEnum.FAILED);
                bookingPayment.setFailureReason("Payment not approved: " + executedPayment.getState());
            }

            return bookingPaymentRepository.save(bookingPayment);

        } catch (PayPalRESTException e) {
            log.error("PayPal payment execution failed: {}", e.getMessage());
            bookingPayment.setStatus(Constants.PaymentStatusEnum.FAILED);
            bookingPayment.setFailureReason(e.getMessage());
            return bookingPaymentRepository.save(bookingPayment);
        }
    }
}
