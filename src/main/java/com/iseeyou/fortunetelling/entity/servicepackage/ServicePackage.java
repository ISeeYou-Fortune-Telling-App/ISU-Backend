package com.iseeyou.fortunetelling.entity.servicepackage;

import com.iseeyou.fortunetelling.entity.AbstractBaseEntity;
import com.iseeyou.fortunetelling.entity.booking.Booking;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.util.Constants;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name="service_package")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@AttributeOverrides({
        @AttributeOverride(name = "id", column = @Column(name = "package_id", nullable = false)),
})
@SQLDelete(sql = "UPDATE service_package SET deleted_at = NOW() WHERE package_id = ?")
@Where(clause = "deleted_at IS NULL")
public class ServicePackage extends AbstractBaseEntity {
    @Column(name = "package_title", nullable = false, length = 100)
    private String packageTitle;

    @Column(name = "package_content", length = 1000)
    private String packageContent;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "status", length = 20)
    private Constants.PackageStatusEnum status;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "like_count")
    @Builder.Default
    private Long likeCount = 0L;

    @Column(name = "dislike_count")
    @Builder.Default
    private Long dislikeCount = 0L;

    @Column(name = "comment_count")
    @Builder.Default
    private Long commentCount = 0L;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "servicePackage", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PackageCategory> packageCategories = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seer_id")
    private User seer;

    @OneToMany(mappedBy = "servicePackage", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PackageInteraction> packageInteractions = new HashSet<>();

    @OneToMany(mappedBy = "servicePackage", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ServiceReview> serviceReviews = new HashSet<>();

    @OneToMany(mappedBy = "servicePackage", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Booking> bookings = new HashSet<>();
}
