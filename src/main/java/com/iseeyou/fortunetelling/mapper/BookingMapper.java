package com.iseeyou.fortunetelling.mapper;

import com.iseeyou.fortunetelling.dto.response.booking.BookingPaymentResponse;
import com.iseeyou.fortunetelling.dto.response.booking.BookingResponse;
import com.iseeyou.fortunetelling.entity.booking.Booking;
import com.iseeyou.fortunetelling.entity.booking.BookingPayment;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
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
                        Double avgRating = null;
                        if (source.getServicePackage().getSeer().getSeerProfile() != null) {
                            avgRating = source.getServicePackage().getSeer().getSeerProfile().getAvgRating();
                        }

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

                    // Map booking payment information
                    if (source.getBookingPayments() != null && !source.getBookingPayments().isEmpty()) {
                        BookingResponse.BookingPaymentInfo[] paymentInfos = source.getBookingPayments().stream()
                                .map(payment -> BookingResponse.BookingPaymentInfo.builder()
                                        .amount(payment.getAmount())
                                        .paymentMethod(payment.getPaymentMethod())
                                        .paymentStatus(payment.getStatus())
                                        .paymentTime(payment.getCreatedAt())
                                        .approvalUrl(payment.getApprovalUrl())
                                        .failureReason(payment.getFailureReason())
                                        .build())
                                .toArray(BookingResponse.BookingPaymentInfo[]::new);
                        destination.setBookingPaymentInfos(paymentInfos);

                        // Set redirectUrl from the latest booking payment's approval URL
                        Optional<BookingPayment> latestPayment = source.getBookingPayments().stream()
                                .max(Comparator.comparing(BookingPayment::getCreatedAt));
                        if (latestPayment.isPresent() && latestPayment.get().getApprovalUrl() != null) {
                            destination.setRedirectUrl(latestPayment.get().getApprovalUrl());
                        }
                    }

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
