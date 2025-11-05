package com.iseeyou.fortunetelling.entity.servicepackage;

import com.iseeyou.fortunetelling.entity.AbstractBaseEntity;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.util.Constants;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="package_interaction", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "package_id"})
})
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PackageInteraction extends AbstractBaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(name = "interaction_type", nullable = false)
    private Constants.InteractionTypeEnum interactionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", nullable = false)
    private ServicePackage servicePackage;
}
