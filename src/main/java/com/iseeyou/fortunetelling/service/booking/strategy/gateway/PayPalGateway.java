package com.iseeyou.fortunetelling.service.booking.strategy.gateway;

import com.iseeyou.fortunetelling.util.Constants;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class PayPalGateway {
    private final APIContext apiContext;

    @Value("${SUCCESS_REDIRECT_URL}")
    private String successUrl;

    @Value("${CANCELED_REDIRECT_URL}")
    private String cancelUrl;

    @Transactional
    public Payment createPaymentWithPayPal(
            Double total,
            String bookingId) throws PayPalRESTException {

        Amount amount = new Amount();
        amount.setCurrency("USD");
        amount.setTotal(String.format("%.2f", total));

        Transaction transaction = new Transaction();
        transaction.setDescription("Thanh toán cho lịch hẹn mã số " + bookingId);
        transaction.setAmount(amount);

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        Payer payer = new Payer();
        payer.setPaymentMethod(Constants.PaymentMethodEnum.PAYPAL.getValue());

        Payment payment = new Payment();
        payment.setIntent("SALE"); // "sale" cho thanh toán ngay
        payment.setPayer(payer);
        payment.setTransactions(transactions);
        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(cancelUrl);
        redirectUrls.setReturnUrl(successUrl);

        payment.setRedirectUrls(redirectUrls);

        return payment.create(apiContext);
    }

    // Xác nhận thanh toán sau khi người dùng hoàn tất trên PayPal
    @Transactional
    public Payment executePayment(String paymentId, String payerId) throws PayPalRESTException {
        Payment payment = new Payment();
        payment.setId(paymentId);
        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(payerId);
        Payment completePayment = null;
        try {
            // Thực hiện giao dịch
            completePayment = payment.execute(apiContext, paymentExecution);
        } catch (PayPalRESTException e) {
            log.warn("Giao dịch Paypal thất bại: " + e.getMessage(), e);
            throw e;
        }

        return completePayment;
    }

    /**
     * Hoàn tiền cho một Sale transaction trên PayPal
     * @param saleId ID của sale transaction cần hoàn tiền
     * @param amount Số tiền cần hoàn (null = hoàn toàn bộ)
     * @return DetailedRefund object chứa thông tin hoàn tiền
     * @throws PayPalRESTException nếu có lỗi từ PayPal API
     */
    @Transactional
    public DetailedRefund refundSale(String saleId, Double amount) throws PayPalRESTException {
        try {
            // Lấy thông tin Sale transaction
            Sale sale = Sale.get(apiContext, saleId);
            
            // Tạo refund request
            RefundRequest refundRequest = new RefundRequest();
            
            if (amount != null && amount > 0) {
                // Partial refund
                Amount refundAmount = new Amount();
                refundAmount.setCurrency("USD");
                refundAmount.setTotal(String.format("%.2f", amount));
                refundRequest.setAmount(refundAmount);
                log.info("Processing partial refund of ${} for sale {}", amount, saleId);
            } else {
                // Full refund (no amount = refund entire transaction)
                log.info("Processing full refund for sale {}", saleId);
            }
            
            // Thực hiện hoàn tiền
            DetailedRefund refund = sale.refund(apiContext, refundRequest);
            
            log.info("PayPal refund successful. Refund ID: {}, Status: {}, Amount: {}", 
                    refund.getId(), refund.getState(), 
                    refund.getAmount() != null ? refund.getAmount().getTotal() : "full");
            
            return refund;
            
        } catch (PayPalRESTException e) {
            log.error("PayPal refund failed for sale {}: {} - {}", 
                    saleId, e.getMessage(), e.getDetails());
            throw e;
        }
    }
}
