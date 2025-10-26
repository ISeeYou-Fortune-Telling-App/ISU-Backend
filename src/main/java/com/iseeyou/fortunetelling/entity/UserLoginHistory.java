package com.iseeyou.fortunetelling.entity;

import com.iseeyou.fortunetelling.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_login_history")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@AttributeOverrides({
        @AttributeOverride(name = "id", column = @Column(name = "login_history_id", nullable = false)),
})
public class UserLoginHistory extends AbstractBaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "device_info")
    private String deviceInfo;

    @Column(name = "location")
    private String location;

    @Column(name = "device_fingerprint")
    private String deviceFingerprint;

    @Column(name = "is_trusted_device")
    @Builder.Default
    private Boolean isTrustedDevice = false;

    @Column(name = "login_time", nullable = false)
    private LocalDateTime loginTime;

    @Column(name = "login_success", nullable = false)
    @Builder.Default
    private Boolean loginSuccess = true;

    @Column(name = "failure_reason")
    private String failureReason;
}

