package com.iseeyou.fortunetelling.entity.booking;

import com.iseeyou.fortunetelling.entity.AbstractBaseEntity;
import com.iseeyou.fortunetelling.entity.servicepackage.ServicePackage;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.util.Constants;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="booking")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@AttributeOverrides({
        @AttributeOverride(name = "id", column = @Column(name = "booking_id", nullable = false)),
})
public class Booking extends AbstractBaseEntity {
    @Column(name = "scheduled_time", nullable = false)
    private LocalDateTime scheduledTime;

    @Column(name = "status", nullable = false, length = 20)
    private Constants.BookingStatusEnum status;

    @Column(name = "additional_note", length = 1000)
    private String additionalNote;

    // Review fields (1-1 relationship: booking can have one review)
    @Column(name = "rating", precision = 2, scale = 1)
    private BigDecimal rating; // Range: 1.0 - 5.0, nullable (user may not review)

    @Column(name = "comment", length = 1000)
    private String comment; // Optional review comment

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt; // When the review was submitted

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_package_id", nullable = false)
    private ServicePackage servicePackage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<BookingPayment> bookingPayments = new ArrayList<>();
}
