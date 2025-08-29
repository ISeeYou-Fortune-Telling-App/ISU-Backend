package com.iseeyou.fortunetelling.entity.booking;

import com.iseeyou.fortunetelling.entity.AbstractBaseEntity;
import com.iseeyou.fortunetelling.util.Constants;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="booking_payment")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@AttributeOverrides({
        @AttributeOverride(name = "id", column = @Column(name = "booking_payment_id", nullable = false)),
})
public class BookingPayment extends AbstractBaseEntity {
    @Column(name = "payment_method", nullable = false, length = 50)
    private Constants.PaymentMethodEnum paymentMethod;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "status", nullable = false, length = 20)
    private Constants.PaymentStatusEnum status;

    @Column(name = "transaction_id", length = 1000)
    private String transactionId;

    @Column(name = "approval_url", length = 1000)
    private String approvalUrl;

    @Column(name = "failure_reason", length = 1000)
    private String failureReason;

    @Column(name = "extra_info")
    private String extraInfo;

    // Relationship: Many BookingPayments to One Booking
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;
}
