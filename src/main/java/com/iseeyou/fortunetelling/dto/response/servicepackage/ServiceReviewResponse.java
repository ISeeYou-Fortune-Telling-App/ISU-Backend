package com.iseeyou.fortunetelling.dto.response.servicepackage;

import com.iseeyou.fortunetelling.dto.response.BaseDataResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ServiceReviewResponse extends BaseDataResponse {
    private String content;
    private UUID userId;
    private String userName;
    private String userAvatarUrl;
}
