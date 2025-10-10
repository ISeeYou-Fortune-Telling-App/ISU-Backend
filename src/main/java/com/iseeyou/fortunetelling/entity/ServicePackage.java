package com.iseeyou.fortunetelling.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "service_package")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@AttributeOverrides({
        @AttributeOverride(name = "id", column = @Column(name = "package_id", nullable = false)),
})
public class ServicePackage extends AbstractBaseEntity {

    @Column(name = "seer_id", nullable = false)
    private String seerId;

    @Column(name = "package_title", length = 100, nullable = false)
    private String packageTitle;

    @Column(name = "package_content", length = 1000)
    private String packageContent;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "price", nullable = false)
    private Double price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ServicePackageStatus status = ServicePackageStatus.AVAILABLE;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    public enum ServicePackageStatus {
        AVAILABLE, CLOSED, HAVE_REPORT, HIDDEN
    }
}
