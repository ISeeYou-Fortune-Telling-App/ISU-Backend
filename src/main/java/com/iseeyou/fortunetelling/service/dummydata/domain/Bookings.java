package com.iseeyou.fortunetelling.service.dummydata.domain;

import com.iseeyou.fortunetelling.entity.booking.Booking;
import com.iseeyou.fortunetelling.entity.booking.BookingPayment;
import com.iseeyou.fortunetelling.entity.servicepackage.ServicePackage;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.repository.booking.BookingPaymentRepository;
import com.iseeyou.fortunetelling.repository.booking.BookingRepository;
import com.iseeyou.fortunetelling.repository.servicepackage.ServicePackageRepository;
import com.iseeyou.fortunetelling.repository.user.UserRepository;
import com.iseeyou.fortunetelling.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class Bookings {

    private final BookingRepository bookingRepository;
    private final BookingPaymentRepository bookingPaymentRepository;
    private final ServicePackageRepository servicePackageRepository;
    private final UserRepository userRepository;

    private final Random random = new Random();

    @Transactional
    public void createDummyData() {
        log.info("Bắt đầu tạo dummy data cho Bookings...");

        // Get existing users and service packages
        List<User> customers = userRepository.findAllByRole(Constants.RoleEnum.CUSTOMER);
        List<ServicePackage> servicePackages = servicePackageRepository.findAll();

        if (customers.isEmpty() || servicePackages.isEmpty()) {
            log.warn("Không có customer hoặc service package để tạo bookings");
            return;
        }

        // Create bookings
        List<Booking> createdBookings = createBookings(customers, servicePackages);

        // Create booking payments
        createBookingPayments(createdBookings);


        // Create booking reviews
        createBookingReviews(createdBookings, customers);

        log.info("Hoàn thành tạo dummy data cho Bookings.");
    }

    private List<Booking> createBookings(List<User> customers, List<ServicePackage> servicePackages) {
        String[] additionalNotes = {
            "Tôi muốn tư vấn về tình duyên trong năm nay, đặc biệt là khả năng gặp được người phù hợp.",
            "Xin thầy xem giúp em về sự nghiệp, em đang có cơ hội chuyển công ty mới.",
            "Em muốn biết về vận may tài chính của mình trong thời gian tới.",
            "Xin thầy tư vấn về việc học hành của con em, năm nay con sắp thi đại học.",
            "Tôi đang có một số vấn đề trong gia đình, mong thầy có thể cho lời khuyên.",
            "Em muốn biết thời điểm nào thích hợp để mở cửa hàng kinh doanh.",
            "Xin thầy xem giúp về sức khỏe, gần đây em hay cảm thấy mệt mỏi.",
            "Tôi muốn tư vấn về việc mua nhà, không biết có nên mua trong năm nay không.",
            "Em đang phân vân về một mối quan hệ, không biết có nên tiếp tục hay không.",
            "Xin thầy tư vấn về hướng phát triển sự nghiệp phù hợp nhất với bản mệnh của em.",
            "Tôi muốn biết về vận mệnh tổng quát của mình trong năm Giáp Thìn này.",
            "Em có dự định du học, xin thầy xem có thuận lợi không.",
            "Gia đình em đang có mâu thuẫn, mong thầy có thể tư vạn cách hóa giải.",
            "Em muốn biết về khả năng sinh con trong thời gian tới.",
            "Xin thầy xem giúp về việc đầu tư, em đang có ý định mua cổ phiếu.",
            null // Some bookings may not have additional notes
        };

        // Create bookings for the past 6 months to current time
        LocalDateTime startDate = LocalDateTime.now().minusMonths(6);
        LocalDateTime endDate = LocalDateTime.now().plusDays(30); // Some future bookings

        for (int i = 0; i < 50; i++) { // Create 50 bookings
            User customer = customers.get(random.nextInt(customers.size()));
            ServicePackage servicePackage = servicePackages.get(random.nextInt(servicePackages.size()));

            // Random scheduled time between start and end date
            LocalDateTime scheduledTime = generateRandomDateTime(startDate, endDate);

            Constants.BookingStatusEnum status = generateBookingStatus(scheduledTime);
            String additionalNote = additionalNotes[random.nextInt(additionalNotes.length)];

            Booking booking = Booking.builder()
                    .scheduledTime(scheduledTime)
                    .status(status)
                    .additionalNote(additionalNote)
                    .servicePackage(servicePackage)
                    .customer(customer)
                    .build();

            bookingRepository.save(booking);
            log.info("Đã tạo booking #{} cho customer: {} - package: {}",
                    i + 1, customer.getFullName(), servicePackage.getPackageTitle());
        }

        return bookingRepository.findAll();
    }

    @Transactional
    protected void createBookingPayments(List<Booking> bookings) {
        Constants.PaymentMethodEnum[] paymentMethods = Constants.PaymentMethodEnum.values();

        String[] transactionIds = {
            "PAY_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
            "VNP_" + System.currentTimeMillis(),
            "MOMO_" + UUID.randomUUID().toString().substring(0, 10).toUpperCase(),
            "BANK_" + System.currentTimeMillis(),
            "WALLET_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()
        };

        for (Booking booking : bookings) {
            // Each booking has 1-2 payment records (some might have failed then successful payments)
            int numPayments = 1 + random.nextInt(2);

            for (int i = 0; i < numPayments; i++) {
                Constants.PaymentMethodEnum paymentMethod = paymentMethods[random.nextInt(paymentMethods.length)];
                Constants.PaymentStatusEnum paymentStatus;

                // If this is the second payment, make it successful (retry after failure)
                if (i == 1) {
                    paymentStatus = Constants.PaymentStatusEnum.COMPLETED;
                } else {
                    // Random status for first payment
                    paymentStatus = generatePaymentStatus();
                }

                Double amount = booking.getServicePackage().getPrice();
                String transactionId = null;
                String approvalUrl = null;
                String failureReason = null;

                if (paymentStatus == Constants.PaymentStatusEnum.COMPLETED) {
                    transactionId = transactionIds[random.nextInt(transactionIds.length)];
                    if (paymentMethod == Constants.PaymentMethodEnum.PAYPAL) {
                        approvalUrl = "https://www.sandbox.paypal.com/checkoutnow?token=" + UUID.randomUUID().toString();
                    }
                } else if (paymentStatus == Constants.PaymentStatusEnum.FAILED) {
                    failureReason = generateFailureReason();
                }

                BookingPayment payment = BookingPayment.builder()
                        .paymentMethod(paymentMethod)
                        .amount(amount)
                        .status(paymentStatus)
                        .transactionId(transactionId)
                        .approvalUrl(approvalUrl)
                        .failureReason(failureReason)
                        .extraInfo(generateExtraInfo(paymentMethod))
                        .booking(booking)
                        .build();

                bookingPaymentRepository.save(payment);
            }
        }

        log.info("Đã tạo booking payments cho tất cả bookings");
    }

    private void createBookingReviews(List<Booking> bookings, List<User> customers) {
        String[] reviewComments = {
            "Thầy xem rất chính xác và tư vấn rất chi tiết. Em rất hài lòng với buổi tư vấn.",
            "Dịch vụ tuyệt vời! Thầy giải thích rất rõ ràng và dễ hiểu. Sẽ quay lại lần sau.",
            "Cảm ơn thầy đã tư vấn tận tình. Những lời khuyên rất hữu ích cho cuộc sống của em.",
            "Thầy rất chuyên nghiệp và nhiệt tình. Buổi tư vấn đã giúp em có thêm định hướng rõ ràng.",
            "Xem tướng rất chuẩn, đúng như thực tế mà em đang trải qua. Cảm ơn thầy!",
            "Dịch vụ chất lượng cao với giá cả hợp lý. Em rất recommend cho mọi người.",
            "Thầy tư vấn rất hay và có chiều sâu. Em đã hiểu được nhiều điều về bản thân mình.",
            "Buổi tư vấn rất bổ ích, giúp em có cái nhìn tích cực hơn về tương lai.",
            "Thầy rất kiên nhẫn giải đáp mọi thắc mắc của em. Dịch vụ đáng đồng tiền bát gạo.",
            "Cảm ơn thầy đã cho em những lời khuyên quý báu. Em sẽ áp dụng vào cuộc sống.",
            "Thầy xem rất tâm đắc và có tâm huyết. Em cảm thấy được động viên rất nhiều.",
            "Dịch vụ tốt, thầy rất am hiểu và có kinh nghiệm. Sẽ giới thiệu cho bạn bè.",
            "Buổi tư vấn đã giúp em giải tỏa được những lo lắng trong lòng. Cảm ơn thầy!",
            "Thầy tư vấn rất chu đáo và chân thành. Em cảm thấy được an ủi và có thêm hy vọng.",
            "Excellent service! Thầy rất professional và friendly. Highly recommended!",
            "Thời gian tư vấn hợp lý, nội dung chi tiết. Em rất satisfied với buổi session này."
        };

        // Only create reviews for completed bookings
        List<Booking> completedBookings = bookings.stream()
                .filter(booking -> booking.getStatus() == Constants.BookingStatusEnum.COMPLETED)
                .toList();

        for (Booking booking : completedBookings) {
            // 70% chance of having a review
            if (random.nextDouble() < 0.7) {
                String comment = reviewComments[random.nextInt(reviewComments.length)];
                double rating = 3.0 + (random.nextInt(21) / 10.0); // Rating between 3.0-5.0 (3.0, 3.1, 3.2, ..., 5.0)

                // Set review directly in booking entity
                booking.setRating(BigDecimal.valueOf(rating));
                booking.setComment(comment);
                booking.setReviewedAt(LocalDateTime.now().minusDays(random.nextInt(30))); // Reviewed within last 30 days

                bookingRepository.save(booking);
            }
        }

        log.info("Đã tạo booking reviews cho các bookings đã hoàn thành");
    }

    // Helper methods
    private LocalDateTime generateRandomDateTime(LocalDateTime start, LocalDateTime end) {
        long startEpoch = start.toEpochSecond(java.time.ZoneOffset.UTC);
        long endEpoch = end.toEpochSecond(java.time.ZoneOffset.UTC);
        long randomEpoch = startEpoch + (long) (random.nextDouble() * (endEpoch - startEpoch));
        return LocalDateTime.ofEpochSecond(randomEpoch, 0, java.time.ZoneOffset.UTC);
    }

    private Constants.BookingStatusEnum generateBookingStatus(LocalDateTime scheduledTime) {
        LocalDateTime now = LocalDateTime.now();

        if (scheduledTime.isAfter(now)) {
            // Future bookings - mostly pending or confirmed
            return random.nextBoolean() ? Constants.BookingStatusEnum.PENDING : Constants.BookingStatusEnum.CONFIRMED;
        } else {
            // Past bookings - mostly completed, some cancelled
            double rand = random.nextDouble();
            if (rand < 0.8) {
                return Constants.BookingStatusEnum.COMPLETED;
            } else if (rand < 0.95) {
                return Constants.BookingStatusEnum.CANCELED;
            } else {
                return Constants.BookingStatusEnum.FAILED;
            }
        }
    }

    private Constants.PaymentStatusEnum generatePaymentStatus() {
        double rand = random.nextDouble();
        if (rand < 0.8) {
            return Constants.PaymentStatusEnum.COMPLETED;
        } else if (rand < 0.9) {
            return Constants.PaymentStatusEnum.PENDING;
        } else if (rand < 0.95) {
            return Constants.PaymentStatusEnum.FAILED;
        } else {
            return Constants.PaymentStatusEnum.REFUNDED;
        }
    }

    private String generateFailureReason() {
        String[] reasons = {
            "Insufficient funds in account",
            "Card expired or invalid",
            "Payment declined by bank",
            "Network timeout during transaction",
            "Invalid payment credentials",
            "Daily transaction limit exceeded"
        };
        return reasons[random.nextInt(reasons.length)];
    }

    private String generateExtraInfo(Constants.PaymentMethodEnum paymentMethod) {
        return switch (paymentMethod) {
            case VNPAY -> "{\"vnp_BankCode\":\"NCB\",\"vnp_CardType\":\"ATM\"}";
            case MOMO -> "{\"phoneNumber\":\"0987654321\",\"walletType\":\"MOMO\"}";
            case PAYPAL -> "{\"payerEmail\":\"customer@example.com\",\"currency\":\"USD\"}";
//            case BANK_TRANSFER -> "{\"bankCode\":\"VCB\",\"accountNumber\":\"****1234\"}";
            default -> null;
        };
    }
}
