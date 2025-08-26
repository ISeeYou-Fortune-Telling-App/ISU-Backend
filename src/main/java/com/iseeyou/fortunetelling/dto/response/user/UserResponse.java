package com.iseeyou.fortunetelling.dto.response.user;

import com.iseeyou.fortunetelling.dto.response.AbstractBaseDataResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class UserResponse<T> extends AbstractBaseDataResponse {
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
    private T profile;
}
