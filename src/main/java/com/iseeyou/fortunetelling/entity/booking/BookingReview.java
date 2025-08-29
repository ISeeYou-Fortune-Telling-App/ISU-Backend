package com.iseeyou.fortunetelling.entity.booking;

import com.iseeyou.fortunetelling.entity.AbstractBaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="booking_review")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@AttributeOverrides({
        @AttributeOverride(name = "id", column = @Column(name = "booking_review_id", nullable = false)),
})
public class BookingReview extends AbstractBaseEntity {
    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "comment", length = 1000)
    private String comment;

    // Relationship: Many BookingReviews to One Booking
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;
}
