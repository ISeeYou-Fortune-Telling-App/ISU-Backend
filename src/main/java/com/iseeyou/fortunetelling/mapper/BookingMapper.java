package com.iseeyou.fortunetelling.mapper;

import com.iseeyou.fortunetelling.dto.response.booking.BookingPaymentResponse;
import com.iseeyou.fortunetelling.dto.response.booking.BookingResponse;
import com.iseeyou.fortunetelling.entity.booking.Booking;
import com.iseeyou.fortunetelling.entity.booking.BookingPayment;
import com.iseeyou.fortunetelling.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class BookingMapper extends BaseMapper {

    @Autowired
    public BookingMapper(ModelMapper modelMapper) {
        super(modelMapper);
    }

    @Override
    protected void configureCustomMappings() {
        // Map Booking entity to BookingResponse DTO
        modelMapper.typeMap(Booking.class, BookingResponse.class)
                .setPostConverter(context -> {
                    Booking source = context.getSource();
                    BookingResponse destination = context.getDestination();

                    if (source.getCustomer() != null) {
                        BookingResponse.BookingCustomerInfo customerInfo = BookingResponse.BookingCustomerInfo.builder()
                                .id(source.getCustomer().getId())
                                .fullName(source.getCustomer().getFullName())
                                .avatarUrl(source.getCustomer().getAvatarUrl())
                                .build();
                        destination.setCustomer(customerInfo);
                    }

                    if (source.getServicePackage() != null && source.getServicePackage().getSeer() != null) {
                        Double avgRating = 0.0; // Temporarily set to 0

                        BookingResponse.BookingSeerInfo seerInfo = BookingResponse.BookingSeerInfo.builder()
                                .id(source.getServicePackage().getSeer().getId())
                                .fullName(source.getServicePackage().getSeer().getFullName())
                                .avatarUrl(source.getServicePackage().getSeer().getAvatarUrl())
                                .avgRating(avgRating)
                                .build();
                        destination.setSeer(seerInfo);
                    }

                    if (source.getServicePackage() != null) {
                        List<String> categoryNames = source.getServicePackage().getPackageCategories() != null ?
                                source.getServicePackage().getPackageCategories().stream()
                                        .map(pc -> pc.getKnowledgeCategory().getName())
                                        .collect(Collectors.toList()) :
                                List.of();

                        BookingResponse.ServicePackageInfo servicePackageInfo = BookingResponse.ServicePackageInfo.builder()
                                .packageTitle(source.getServicePackage().getPackageTitle())
                                .packageContent(source.getServicePackage().getPackageContent())
                                .price(source.getServicePackage().getPrice())
                                .durationMinutes(source.getServicePackage().getDurationMinutes())
                                .categories(categoryNames)
                                .build();
                        destination.setServicePackage(servicePackageInfo);
                    }

                    // Map booking payment information - only include payments with PaymentType == PAID_PACKAGE
                    BookingResponse.BookingPaymentInfo[] paymentInfos;
                    if (source.getBookingPayments() == null || source.getBookingPayments().isEmpty()) {
                        paymentInfos = new BookingResponse.BookingPaymentInfo[0];
                    } else {
                        // collect to LinkedHashMap to deduplicate by id while preserving order, then map values
                        paymentInfos = source.getBookingPayments().stream()
                                .filter(Objects::nonNull)
                                // only include PAID_PACKAGE (payment_type == 0)
                                .filter(p -> p.getPaymentType() != null && p.getPaymentType() == Constants.PaymentTypeEnum.PAID_PACKAGE)
                                .collect(Collectors.toMap(BookingPayment::getId, p -> p, (a, b) -> a, LinkedHashMap::new))
                                .values().stream()
                                .map(payment -> BookingResponse.BookingPaymentInfo.builder()
                                        .amount(payment.getAmount())
                                        .paymentMethod(payment.getPaymentMethod())
                                        .paymentStatus(payment.getStatus())
                                        .paymentTime(payment.getCreatedAt())
                                        // approvalUrl intentionally removed
                                        .failureReason(payment.getFailureReason())
                                        .build())
                                .toArray(BookingResponse.BookingPaymentInfo[]::new);
                    }
                    destination.setBookingPaymentInfos(paymentInfos);

                    // Debug log to help trace mapping issues at runtime
                    try {
                        log.debug("Booking {} - mapped paymentInfos count: {}", source.getId(), paymentInfos != null ? paymentInfos.length : 0);
                    } catch (Exception ignored) {
                    }

                    // NOTE: redirectUrl removed from response DTO, so we do not set it here.

                    // Map review information
                    if (source.getRating() != null || source.getComment() != null || source.getReviewedAt() != null) {
                        BookingResponse.BookingReviewInfo reviewInfo = BookingResponse.BookingReviewInfo.builder()
                                .rating(source.getRating())
                                .comment(source.getComment())
                                .reviewedAt(source.getReviewedAt())
                                .build();
                        destination.setReview(reviewInfo);
                    }

                    return destination;
                });

        modelMapper.typeMap(BookingPayment.class, BookingPaymentResponse.class)
                .setPostConverter(context -> {
                    BookingPayment source = context.getSource();
                    BookingPaymentResponse destination = context.getDestination();

                    // Map booking ID
                    if (source.getBooking() != null) {
                        destination.setBookingId(source.getBooking().getId());
                    }

                    // Map payment status
                    destination.setPaymentStatus(source.getStatus());

                    // Map customer information from booking
                    if (source.getBooking() != null && source.getBooking().getCustomer() != null) {
                        BookingPaymentResponse.BookingUserInfo customerInfo = BookingPaymentResponse.BookingUserInfo.builder()
                                .fullName(source.getBooking().getCustomer().getFullName())
                                .avatarUrl(source.getBooking().getCustomer().getAvatarUrl())
                                .build();
                        destination.setCustomer(customerInfo);
                    }

                    // Map seer information from service package
                    if (source.getBooking() != null &&
                        source.getBooking().getServicePackage() != null &&
                        source.getBooking().getServicePackage().getSeer() != null) {
                        BookingPaymentResponse.BookingUserInfo seerInfo = BookingPaymentResponse.BookingUserInfo.builder()
                                .fullName(source.getBooking().getServicePackage().getSeer().getFullName())
                                .avatarUrl(source.getBooking().getServicePackage().getSeer().getAvatarUrl())
                                .build();
                        destination.setSeer(seerInfo);
                    }

                    // Map package title
                    if (source.getBooking() != null && source.getBooking().getServicePackage() != null) {
                        destination.setPackageTitle(source.getBooking().getServicePackage().getPackageTitle());
                    }

                    return destination;
                });
    }
}
