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
public class SimpleUserResponse extends AbstractBaseDataResponse {
    private String name;
    private String avatarUrl;
}

