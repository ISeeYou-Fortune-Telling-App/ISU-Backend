package com.iseeyou.fortunetelling.entity.user;

import com.iseeyou.fortunetelling.entity.AbstractBaseEntity;
import com.iseeyou.fortunetelling.util.Constants;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name="user")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@AttributeOverrides({
        @AttributeOverride(name = "id", column = @Column(name = "user_id", nullable = false)),
})
public class User extends AbstractBaseEntity {
    @Column(name = "role", nullable = false)
    private Constants.RoleEnum role;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "phone", nullable = false, unique = true)
    private String phone;

    @Column(name = "gender", nullable = false)
    private String gender;

    @Column(name = "password_hash", nullable = false)
    private String password;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "cover_url")
    private String coverUrl;

    @Column(name = "profile_description", length = 1000)
    private String profileDescription;

    @Column(name = "birth_date")
    private LocalDateTime birthDate;

    @Column(name = "status", nullable = false)
    private Constants.StatusProfileEnum status;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private CustomerProfile customerProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private SeerProfile seerProfile;
}
