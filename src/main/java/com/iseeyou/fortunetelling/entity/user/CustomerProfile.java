package com.iseeyou.fortunetelling.entity.user;

import com.iseeyou.fortunetelling.entity.AbstractBaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="customer_profile")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerProfile extends AbstractBaseEntity {
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "customer_id", referencedColumnName = "user_id")
    private User user;

    @Column(name = "zodiac_sign", nullable = false)
    private String zodiacSign;

    @Column(name = "chinese_zodiac", nullable = false)
    private String chineseZodiac;

    @Column(name = "five_elements", nullable = false)
    private String fiveElements;
}
