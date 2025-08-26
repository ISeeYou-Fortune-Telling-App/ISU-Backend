package com.iseeyou.fortunetelling.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public abstract class AbstractBaseDataResponse {
    private UUID id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
