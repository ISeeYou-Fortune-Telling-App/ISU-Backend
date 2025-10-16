package com.iseeyou.fortunetelling.repository.booking;

import com.iseeyou.fortunetelling.entity.booking.BookingReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BookingReviewRepository extends JpaRepository<BookingReview, UUID>, JpaSpecificationExecutor<BookingReview> {
    Page<BookingReview> findAllByBooking_Id(UUID bookingId, Pageable pageable);
}
