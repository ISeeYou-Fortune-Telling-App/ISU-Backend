package com.iseeyou.fortunetelling.entity.user;

import com.iseeyou.fortunetelling.entity.AbstractBaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="seer_profile")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeerProfile extends AbstractBaseEntity {
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "seer_id", referencedColumnName = "user_id")
    private User user;

    @Column(name = "avg_rating", nullable = false)
    private Double avgRating;

    @Column(name = "total_rates", nullable = false)
    private Integer totalRates;

    @Column(name = "payment_info", length = 500)
    private String paymentInfo;
}
