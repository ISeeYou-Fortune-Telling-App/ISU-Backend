package com.iseeyou.fortunetelling.dto.request.auth;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class SeerRegisterRequest extends RegisterRequest {
    private Set<String> specialityIds;
    private String profileDescription;
    // For multipart/form-data, provide certificates as JSON string; will be parsed in service
    private String certificates;
}
