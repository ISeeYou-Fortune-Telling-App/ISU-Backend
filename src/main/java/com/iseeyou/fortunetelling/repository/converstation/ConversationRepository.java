package com.iseeyou.fortunetelling.repository.converstation;

import com.iseeyou.fortunetelling.entity.Conversation;
import com.iseeyou.fortunetelling.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    Optional<Conversation> findByBookingId(UUID bookingId);
    Page<Conversation> findByBooking_ServicePackage_Seer(User seer, Pageable pageable);
    Page<Conversation> findByBooking_Customer(User customer, Pageable pageable);
}
