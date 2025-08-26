package com.iseeyou.fortunetelling.entity;

import com.iseeyou.fortunetelling.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="service_review")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@AttributeOverrides({
        @AttributeOverride(name = "id", column = @Column(name = "review_id", nullable = false)),
})
public class ServiceReview extends AbstractBaseEntity {
    @Column(name = "comment", length = 1000, nullable = false)
    private String comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", nullable = false)
    private ServicePackage servicePackage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_parent_id")
    private ServiceReview parentReview;

    @OneToMany(mappedBy = "parentReview", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ServiceReview> replies = new ArrayList<>();
}
