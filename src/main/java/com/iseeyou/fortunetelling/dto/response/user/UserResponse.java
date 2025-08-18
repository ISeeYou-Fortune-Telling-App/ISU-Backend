package com.iseeyou.fortunetelling.dto.response.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class UserResponse {
    private String id;
    private String role;
    private String email;
    private String phone;
    private String gender;
    private String fullName;
    private String avatarUrl;
    private String coverUrl;
    private String profileDescription;
    private String birthDate;
    private String status;
    private String createdAt;
    private String updatedAt;
}
