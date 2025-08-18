package com.iseeyou.fortunetelling.entity;

import com.iseeyou.fortunetelling.util.Constants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
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
public class User extends AbstractBaseEntity{
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
}
