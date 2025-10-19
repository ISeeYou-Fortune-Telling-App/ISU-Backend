package com.iseeyou.fortunetelling.repository.booking;

import com.iseeyou.fortunetelling.entity.booking.Booking;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.util.Constants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID>, JpaSpecificationExecutor<Booking> {

    @EntityGraph(attributePaths = {"servicePackage", "customer", "servicePackage.seer","servicePackage.seer.seerProfile", "bookingPayments", "servicePackage.packageCategories.knowledgeCategory"})
    Page<Booking> findAllByCustomer(User customer, Pageable pageable);

    @EntityGraph(attributePaths = {"servicePackage", "customer", "servicePackage.seer", "bookingPayments", "servicePackage.packageCategories.knowledgeCategory"})
    @Query("SELECT b FROM Booking b WHERE b.servicePackage.seer = :seer")
    Page<Booking> findAllBySeer(User seer, Pageable pageable);

    @EntityGraph(attributePaths = {"servicePackage", "customer", "servicePackage.seer", "bookingPayments", "servicePackage.packageCategories.knowledgeCategory"})
    Page<Booking> findAllByCustomerAndStatus(User customer, Constants.BookingStatusEnum status, Pageable pageable);

    @EntityGraph(attributePaths = {"servicePackage", "customer", "servicePackage.seer", "bookingPayments", "servicePackage.packageCategories.knowledgeCategory"})
    @Query("SELECT b FROM Booking b WHERE b.servicePackage.seer = :seer AND b.status = :status")
    Page<Booking> findAllBySeerAndStatus(User seer, Constants.BookingStatusEnum status, Pageable pageable);

    @EntityGraph(attributePaths = {"servicePackage", "customer", "servicePackage.seer", "bookingPayments", "servicePackage.packageCategories.knowledgeCategory"})
    Page<Booking> findAllByStatus(Constants.BookingStatusEnum status, Pageable pageable);

    // Thống kê booking cho seer
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.servicePackage.seer = :seer")
    Long countBySeer(User seer);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.servicePackage.seer = :seer AND b.status = :status")
    Long countBySeerAndStatus(User seer, Constants.BookingStatusEnum status);

    @Query("SELECT COALESCE(SUM(bp.amount), 0.0) FROM Booking b JOIN b.bookingPayments bp " +
           "WHERE b.servicePackage.seer = :seer AND bp.status = :paymentStatus")
    Double getTotalRevenueBySeer(User seer, Constants.PaymentStatusEnum paymentStatus);

    @EntityGraph(attributePaths = {"servicePackage", "customer", "bookingPayments"})
    @Query("SELECT b FROM Booking b WHERE b.servicePackage.seer = :seer ORDER BY b.scheduledTime DESC")
    List<Booking> findRecentBookingsBySeer(User seer, Pageable pageable);
}

