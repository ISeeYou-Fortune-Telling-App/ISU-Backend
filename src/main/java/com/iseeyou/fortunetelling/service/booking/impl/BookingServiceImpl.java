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
import com.iseeyou.fortunetelling.service.chat.ConversationService;
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
    private final ConversationService conversationService;
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
        // Validate that only PayPal is supported temporarily
        if (paymentMethod != Constants.PaymentMethodEnum.PAYPAL) {
            throw new IllegalArgumentException("Currently only PayPal payment method is supported. Please use PAYPAL.");
        }
        
        booking.setStatus(Constants.BookingStatusEnum.PENDING);
        booking.setServicePackage(servicePackageService.findById(packageId.toString()));
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

        newBooking = bookingRepository.save(newBooking);

        // TODO: Create conversation

        return newBooking;
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
        Booking existingBooking = bookingRepository.findById(booking.getId())
                .orElseThrow(() -> new NotFoundException("Booking not found with id: " + booking.getId()));

        // TODO: Check if changed to other time => update conversation

        // check if status changed to CONFIRMED
        boolean statusChangedToConfirmed = !existingBooking.getStatus().equals(booking.getStatus()) &&
                booking.getStatus().equals(Constants.BookingStatusEnum.CONFIRMED);

        // Update booking
        existingBooking.setStatus(booking.getStatus());
        existingBooking.setAdditionalNote(booking.getAdditionalNote());
        existingBooking.setScheduledTime(booking.getScheduledTime());

        Booking updatedBooking = bookingRepository.save(existingBooking);

        // create chat session if booking confirmed
        if (statusChangedToConfirmed) {
            log.info("Booking confirmed, creating chat session for booking: {}", booking.getId());
            conversationService.createChatSession(booking.getId());
        }

        return updatedBooking;
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
