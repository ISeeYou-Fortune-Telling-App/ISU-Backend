package com.iseeyou.fortunetelling.service.booking.impl;

import com.iseeyou.fortunetelling.entity.booking.Booking;
import com.iseeyou.fortunetelling.entity.booking.BookingPayment;
import com.iseeyou.fortunetelling.entity.booking.BookingReview;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.exception.NotFoundException;
import com.iseeyou.fortunetelling.repository.booking.BookingPaymentRepository;
import com.iseeyou.fortunetelling.repository.booking.BookingRepository;
import com.iseeyou.fortunetelling.repository.booking.BookingReviewRepository;
import com.iseeyou.fortunetelling.service.booking.BookingService;
import com.iseeyou.fortunetelling.service.booking.strategy.PaymentStrategy;
import com.iseeyou.fortunetelling.service.servicepackage.ServicePackageService;
import com.iseeyou.fortunetelling.service.user.UserService;
import com.iseeyou.fortunetelling.util.Constants;
import com.paypal.base.rest.PayPalRESTException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BookingReviewRepository bookingReviewRepository;
    private final BookingPaymentRepository bookingPaymentRepository;
    private final UserService userService;
    private final ServicePackageService servicePackageService;
    private final Map<Constants.PaymentMethodEnum, PaymentStrategy> paymentStrategies;

    @Override
    @Transactional(readOnly = true)
    public Page<Booking> getBookingsByMe(Pageable pageable) {
        User currentUser = userService.getUser();
        if (currentUser.getRole().equals(Constants.RoleEnum.CUSTOMER)) {
            return bookingRepository.findAllByCustomer(currentUser, pageable);
        } else if (currentUser.getRole().equals(Constants.RoleEnum.SEER)) {
            return bookingRepository.findAllBySeer(currentUser, pageable);
        } else {
            return bookingRepository.findAll(pageable);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Booking> getBookingsByMeAndStatus(Constants.BookingStatusEnum status, Pageable pageable) {
        User currentUser = userService.getUser();
        if (currentUser.getRole().equals(Constants.RoleEnum.CUSTOMER)) {
            return bookingRepository.findAllByCustomerAndStatus(currentUser, status, pageable);
        } else if (currentUser.getRole().equals(Constants.RoleEnum.SEER)) {
            return bookingRepository.findAllBySeerAndStatus(currentUser, status, pageable);
        } else {
            return bookingRepository.findAllByStatus(status, pageable);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Booking findById(UUID id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingReview> findAllReviewByBookingId(UUID id, Pageable pageable) {
        return bookingReviewRepository.findAllByBooking_Id(id, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingPayment> findAllByPaymentMethod(Constants.PaymentMethodEnum paymentMethodEnum, Pageable pageable) {
        return bookingPaymentRepository.findAllByPaymentMethod(paymentMethodEnum, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingPayment> findAllByStatus(Constants.PaymentStatusEnum statusEnum, Pageable pageable) {
        return bookingPaymentRepository.findAllByStatus(statusEnum, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingPayment> findAllBookingPayments(Pageable pageable) {
        return bookingPaymentRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingPayment findPaymentById(UUID id) {
        return bookingPaymentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("BookingPayment not found with id: " + id));
    }

    @Override
    @Transactional
    public Booking createBooking(Booking booking, UUID packageId, Constants.PaymentMethodEnum paymentMethod) {
        booking.setStatus(Constants.BookingStatusEnum.PENDING);
        booking.setServicePackage(servicePackageService.findById(packageId));
        User customer = userService.getUser();
        booking.setCustomer(customer);

        Booking newBooking = bookingRepository.save(booking);

        try {
            BookingPayment bookingPayment = createBookingPayment(newBooking, paymentMethod);
            newBooking.getBookingPayments().add(bookingPayment);
        } catch (PayPalRESTException e) {
            log.error("Error creating PayPal payment: {}", e.getMessage());
            newBooking.setStatus(Constants.BookingStatusEnum.FAILED);

            throw new RuntimeException("Error creating payment", e);
        }

        return bookingRepository.save(newBooking);
    }

    @Override
    @Transactional
    public BookingPayment executePayment(Constants.PaymentMethodEnum paymentMethod, Map<String, Object> paymentParams) {
        PaymentStrategy paymentStrategy = paymentStrategies.get(paymentMethod);
        if (paymentStrategy == null) {
            throw new IllegalArgumentException("Unsupported payment method: " + paymentMethod);
        }
        return paymentStrategy.executePayment(paymentParams);
    }

    @Transactional
    protected BookingPayment createBookingPayment(Booking booking, Constants.PaymentMethodEnum paymentMethod) throws PayPalRESTException {
        PaymentStrategy paymentStrategy = paymentStrategies.get(paymentMethod);
        if (paymentStrategy == null) {
            throw new IllegalArgumentException("Unsupported payment method: " + paymentMethod);
        }
        return paymentStrategy.pay(booking);
    }

    @Override
    @Transactional
    public Booking updateBooking(Booking booking) {
        return null;
    }

    @Override
    @Transactional
    public void deleteBooking(UUID id) {
        Booking existingBooking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking not found with id: " + id));
        bookingRepository.delete(existingBooking);
    }

    @Override
    @Transactional
    public Booking refundBooking(UUID id) {
        return null;
    }
}
