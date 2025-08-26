package com.iseeyou.fortunetelling.dto.request.servicepackage;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ServiceReviewCreateRequest {
    private String comment;
    private UUID servicePackageId;
    private UUID commentParentId;
}
