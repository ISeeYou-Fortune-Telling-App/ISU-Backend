package com.iseeyou.fortunetelling.dto.internal;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class UserDataResponse {
    private UUID id;
    private String fullName;
    private String avatarUrl;
}
