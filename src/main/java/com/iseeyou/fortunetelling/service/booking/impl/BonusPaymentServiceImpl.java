package com.iseeyou.fortunetelling.service.booking.impl;

import com.iseeyou.fortunetelling.entity.booking.BookingPayment;
import com.iseeyou.fortunetelling.entity.user.SeerProfile;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.exception.NotFoundException;
import com.iseeyou.fortunetelling.repository.booking.BookingPaymentRepository;
import com.iseeyou.fortunetelling.repository.user.UserRepository;
import com.iseeyou.fortunetelling.service.booking.BonusPaymentService;
import com.iseeyou.fortunetelling.service.booking.strategy.gateway.PayPalGateway;
import com.iseeyou.fortunetelling.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class BonusPaymentServiceImpl implements BonusPaymentService {

    private final UserRepository userRepository;
    private final BookingPaymentRepository bookingPaymentRepository;
    private final PayPalGateway payPalGateway;

    // Tỷ giá VND sang USD
    private static final double VND_TO_USD = 0.000041;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BookingPayment createBonusPayment(UUID seerId, Double amount, String reason) throws Exception {
        log.info("Processing bonus payment for seer {} with amount {}", seerId, amount);

        // 1. Validate seer exists and has role SEER
        User seer = userRepository.findByIdWithSeerProfile(seerId)
                .orElseThrow(() -> new NotFoundException("Seer not found with ID: " + seerId));

        if (seer.getRole() != Constants.RoleEnum.SEER) {
            throw new IllegalArgumentException("User is not a SEER");
        }

        // 2. Get seer profile and validate PayPal email
        SeerProfile seerProfile = seer.getSeerProfile();
        if (seerProfile == null || seerProfile.getPaypalEmail() == null || seerProfile.getPaypalEmail().isEmpty()) {
            throw new IllegalArgumentException("Seer does not have PayPal email configured");
        }

        String paypalEmail = seerProfile.getPaypalEmail();
        log.info("Seer PayPal email: {}", paypalEmail);

        // 3. Convert amount to USD
        double amountInUSD = amount * VND_TO_USD;
        log.info("Converted amount: {} VND = {} USD", amount, amountInUSD);

        // 4. Create booking payment record first (PENDING status)
        BookingPayment bookingPayment = BookingPayment.builder()
                .seer(seer)
                .booking(null)  // No booking for BONUS type
                .amount(amount)
                .paymentType(Constants.PaymentTypeEnum.BONUS)
                .paymentMethod(Constants.PaymentMethodEnum.PAYPAL)
                .status(Constants.PaymentStatusEnum.PENDING)
                .extraInfo(reason != null ? reason : "Bonus payment")
                .build();

        bookingPayment = bookingPaymentRepository.save(bookingPayment);
        log.info("Created bonus payment record with ID: {}", bookingPayment.getId());

        try {
            // 5. Execute PayPal payout
            // TODO: Sau này khi làm xong push notification, sử dụng reason để gửi thông báo tới seer
            Map<String, Object> payoutResponse = payPalGateway.payoutToSeer(
                    paypalEmail,
                    amountInUSD,
                    bookingPayment.getId().toString()
            );

            // 6. Extract payout batch ID from response
            Map<String, Object> batchHeader = (Map<String, Object>) payoutResponse.get("batch_header");
            String payoutBatchId = batchHeader != null ? (String) batchHeader.get("payout_batch_id") : null;
            String batchStatus = batchHeader != null ? (String) batchHeader.get("batch_status") : null;

            log.info("PayPal payout batch created. Batch ID: {}, Status: {}", payoutBatchId, batchStatus);

            // 7. Update payment record with transaction details
            bookingPayment.setTransactionId(payoutBatchId);

            // Update status based on batch status
            if ("SUCCESS".equalsIgnoreCase(batchStatus) || "PENDING".equalsIgnoreCase(batchStatus)) {
                bookingPayment.setStatus(Constants.PaymentStatusEnum.COMPLETED);
            } else {
                bookingPayment.setStatus(Constants.PaymentStatusEnum.FAILED);
                bookingPayment.setFailureReason("Payout batch status: " + batchStatus);
            }

            bookingPayment = bookingPaymentRepository.save(bookingPayment);
            log.info("Bonus payment completed successfully for seer {}", seerId);

            return bookingPayment;

        } catch (Exception e) {
            log.error("Failed to process PayPal payout for seer {}: {}", seerId, e.getMessage(), e);

            // Update payment status to FAILED
            bookingPayment.setStatus(Constants.PaymentStatusEnum.FAILED);
            bookingPayment.setFailureReason(e.getMessage());
            bookingPaymentRepository.save(bookingPayment);

            throw new Exception("Failed to process bonus payment: " + e.getMessage(), e);
        }
    }
}
