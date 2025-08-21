package com.iseeyou.fortunetelling.entity;

import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.util.Constants;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name="certificate")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@AttributeOverrides({
        @AttributeOverride(name = "id", column = @Column(name = "certificate_id", nullable = false)),
})
public class Certificate extends AbstractBaseEntity {
    @Column(name = "certificate_name", nullable = false, length = 100)
    private String certificateName;

    @Column(name = "certificate_description", length = 1000)
    private String certificateDescription;

    @Column(name = "issued_by", nullable = false, length = 100)
    private String issuedBy;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;

    @Column(name = "certificate_url", length = 500)
    private String certificateUrl;

    @Column(name = "status", length = 20)
    private Constants.CertificateStatusEnum status;

    @Column(name = "decision_date")
    private LocalDateTime decisionDate;

    @Column(name = "decision_reason", length = 500)
    private String decisionReason;

    @OneToMany(mappedBy = "certificate", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CertificateCategory> certificateCategories;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seer_id")
    private User seer;
}
