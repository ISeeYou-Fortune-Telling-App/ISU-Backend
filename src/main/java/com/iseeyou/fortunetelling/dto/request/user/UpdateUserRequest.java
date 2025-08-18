package com.iseeyou.fortunetelling.dto.request.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class UpdateUserRequest {
    private String email;
    private String phone;
    private String gender;
    private String fullName;
    private LocalDateTime birthDate;
    private String profileDescription;
}
