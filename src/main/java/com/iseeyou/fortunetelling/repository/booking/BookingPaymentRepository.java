package com.iseeyou.fortunetelling.repository.booking;

import com.iseeyou.fortunetelling.entity.booking.BookingPayment;
import com.iseeyou.fortunetelling.util.Constants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingPaymentRepository extends JpaRepository<BookingPayment, UUID>, JpaSpecificationExecutor<BookingPayment> {
    @EntityGraph(attributePaths = {"booking", "booking.customer", "booking.servicePackage.seer", "booking.servicePackage" })
    Page<BookingPayment> findAllByPaymentMethod(Constants.PaymentMethodEnum paymentMethod, Pageable pageable);
    @EntityGraph(attributePaths = {"booking", "booking.customer", "booking.servicePackage.seer", "booking.servicePackage"})
    Page<BookingPayment> findAllByStatus(Constants.PaymentStatusEnum status, Pageable pageable);

    @EntityGraph(attributePaths = {"booking", "booking.customer", "booking.servicePackage.seer", "booking.servicePackage"})
    Optional<BookingPayment> findByTransactionId(String transactionId);

    @Override
    @EntityGraph(attributePaths = {"booking", "booking.customer", "booking.servicePackage.seer", "booking.servicePackage"})
    BookingPayment save(BookingPayment bookingPayment);

    @EntityGraph(attributePaths = {"booking", "booking.customer", "booking.servicePackage.seer", "booking.servicePackage"})
    BookingPayment findByExtraInfo(String extraInfo);

    @EntityGraph(attributePaths = {"booking", "booking.customer", "booking.servicePackage.seer", "booking.servicePackage"})
    BookingPayment findByBooking_Id(UUID bookingId);
}
