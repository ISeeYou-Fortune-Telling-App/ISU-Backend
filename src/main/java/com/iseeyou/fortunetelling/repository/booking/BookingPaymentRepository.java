package com.iseeyou.fortunetelling.repository.booking;

import com.iseeyou.fortunetelling.entity.booking.BookingPayment;
import com.iseeyou.fortunetelling.util.Constants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
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

    // Added methods: find payments by seer (payments for bookings whose servicePackage.seer.id = ?) and by customer
    @EntityGraph(attributePaths = {"booking", "booking.customer", "booking.servicePackage.seer", "booking.servicePackage"})
    Page<BookingPayment> findAllByBooking_ServicePackage_Seer_Id(UUID seerId, Pageable pageable);

    @EntityGraph(attributePaths = {"booking", "booking.customer", "booking.servicePackage.seer", "booking.servicePackage"})
    Page<BookingPayment> findAllByBooking_Customer_Id(UUID customerId, Pageable pageable);

    // Optional: filter by specific package and seer
    @EntityGraph(attributePaths = {"booking", "booking.customer", "booking.servicePackage.seer", "booking.servicePackage"})
    Page<BookingPayment> findAllByBooking_ServicePackage_IdAndBooking_ServicePackage_Seer_Id(UUID packageId, UUID seerId, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"booking", "booking.customer", "booking.servicePackage.seer", "booking.servicePackage"})
    Page<BookingPayment> findAll(Pageable pageable);

    // Query for daily revenue calculation
    @Query("SELECT bp FROM BookingPayment bp WHERE bp.status = :status " +
           "AND bp.paymentType IN :paymentTypes " +
           "AND DATE(bp.createdAt) = DATE(:date)")
    List<BookingPayment> findPaymentsByStatusAndTypesAndDate(
            @Param("status") Constants.PaymentStatusEnum status,
            @Param("paymentTypes") List<Constants.PaymentTypeEnum> paymentTypes,
            @Param("date") LocalDateTime date
    );

    // Query methods for admin stats
    @Query("SELECT bp FROM BookingPayment bp WHERE bp.paymentType = :paymentType AND bp.status = :status")
    List<BookingPayment> findAllByPaymentTypeAndStatus(
            @Param("paymentType") Constants.PaymentTypeEnum paymentType,
            @Param("status") Constants.PaymentStatusEnum status
    );

    @Query("SELECT COUNT(DISTINCT bp.booking.id) FROM BookingPayment bp " +
           "WHERE bp.paymentType = :paymentType AND bp.status = :status AND bp.booking IS NOT NULL")
    Long countDistinctBookingsByPaymentTypeAndStatus(
            @Param("paymentType") Constants.PaymentTypeEnum paymentType,
            @Param("status") Constants.PaymentStatusEnum status
    );

    @Query("SELECT bp FROM BookingPayment bp WHERE bp.status = :status")
    List<BookingPayment> findAllByStatusList(@Param("status") Constants.PaymentStatusEnum status);
}
