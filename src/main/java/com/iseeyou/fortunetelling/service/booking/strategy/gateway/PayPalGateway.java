package com.iseeyou.fortunetelling.service.booking.strategy.gateway;

import com.iseeyou.fortunetelling.util.Constants;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class PayPalGateway {
    private final APIContext apiContext;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${SUCCESS_REDIRECT_URL}")
    private String successUrl;

    @Value("${CANCELED_REDIRECT_URL}")
    private String cancelUrl;

    @Value("${PAYPAL_MODE:sandbox}")
    private String paypalMode;

    @Value("${CLIENT_ID}")
    private String clientId;

    @Value("${SECRET_KEY}")
    private String clientSecret;

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
        try {
            // Thực hiện giao dịch
            return payment.execute(apiContext, paymentExecution);
        } catch (PayPalRESTException e) {
            log.warn("Giao dịch Paypal thất bại: " + e.getMessage(), e);
            throw e;
        }
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

    /**
     * Lấy fresh access token từ PayPal OAuth API
     * @return Access token string
     * @throws Exception nếu không lấy được token
     */
    private String getAccessToken() throws Exception {
        try {
            String baseUrl = "sandbox".equals(paypalMode)
                ? "https://api-m.sandbox.paypal.com"
                : "https://api-m.paypal.com";
            String tokenUrl = baseUrl + "/v1/oauth2/token";

            // Tạo Basic Auth header với clientId:clientSecret
            String auth = clientId + ":" + clientSecret;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", "Basic " + encodedAuth);

            // Request body
            String requestBody = "grant_type=client_credentials";

            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

            // Gửi request
            ResponseEntity<Map> response = restTemplate.exchange(
                tokenUrl,
                HttpMethod.POST,
                requestEntity,
                Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("access_token")) {
                String accessToken = (String) responseBody.get("access_token");
                log.debug("Successfully obtained fresh PayPal access token");
                return accessToken;
            }

            throw new Exception("Failed to obtain access token from PayPal");

        } catch (Exception e) {
            log.error("Failed to get PayPal access token: {}", e.getMessage(), e);
            throw new Exception("Failed to authenticate with PayPal: " + e.getMessage(), e);
        }
    }

    /**
     * Chuyển tiền cho seer thông qua PayPal Payouts API (sử dụng REST API trực tiếp)
     * @param paypalEmail Email PayPal của seer nhận tiền
     * @param amountInUSD Số tiền cần chuyển (USD)
     * @param bookingId ID của booking để ghi chú
     * @return Map chứa thông tin payout response
     * @throws Exception nếu có lỗi từ PayPal API
     */
    @Transactional
    public Map<String, Object> payoutToSeer(String paypalEmail, Double amountInUSD, String bookingId) throws Exception {
        try {
            log.info("Processing payout of ${} to PayPal email {} for booking {}", amountInUSD, paypalEmail, bookingId);

            // Lấy fresh access token
            String accessToken = getAccessToken();

            // Xác định URL dựa trên mode
            String baseUrl = "sandbox".equals(paypalMode)
                ? "https://api-m.sandbox.paypal.com"
                : "https://api-m.paypal.com";
            String payoutUrl = baseUrl + "/v1/payments/payouts";

            // Tạo request body
            Map<String, Object> payoutRequest = new HashMap<>();

            // Sender batch header
            Map<String, String> senderBatchHeader = new HashMap<>();
            senderBatchHeader.put("sender_batch_id", "PAYOUT-" + bookingId + "-" + System.currentTimeMillis());
            senderBatchHeader.put("email_subject", "Payment for booking " + bookingId);
            senderBatchHeader.put("email_message", "You have received a payment for completed booking.");
            payoutRequest.put("sender_batch_header", senderBatchHeader);

            // Payout items
            List<Map<String, Object>> items = new ArrayList<>();
            Map<String, Object> item = new HashMap<>();
            item.put("recipient_type", "EMAIL");
            item.put("amount", Map.of(
                "value", String.format("%.2f", amountInUSD),
                "currency", "USD"
            ));
            item.put("receiver", paypalEmail);
            item.put("note", "Payment for completed booking " + bookingId);
            item.put("sender_item_id", "ITEM-" + bookingId);
            items.add(item);

            payoutRequest.put("items", items);

            // Tạo headers với fresh access token
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payoutRequest, headers);

            // Gửi request
            ResponseEntity<Map> response = restTemplate.exchange(
                payoutUrl,
                HttpMethod.POST,
                requestEntity,
                Map.class
            );

            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null) {
                Map<String, Object> batchHeader = (Map<String, Object>) responseBody.get("batch_header");
                log.info("PayPal payout successful. Batch ID: {}, Status: {}",
                        batchHeader != null ? batchHeader.get("payout_batch_id") : "N/A",
                        batchHeader != null ? batchHeader.get("batch_status") : "N/A");
            }

            return responseBody;

        } catch (Exception e) {
            log.error("PayPal payout failed for booking {} to email {}: {}",
                    bookingId, paypalEmail, e.getMessage(), e);
            throw e;
        }
    }
}
