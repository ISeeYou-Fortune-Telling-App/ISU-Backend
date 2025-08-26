package com.iseeyou.fortunetelling.dto.response.servicepackage;

import com.iseeyou.fortunetelling.dto.response.AbstractBaseDataResponse;
import com.iseeyou.fortunetelling.util.Constants;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ServicePackageResponse extends AbstractBaseDataResponse {
    private String packageTitle;
    private String packageContent;
    private Integer durationMinutes;
    private Double price;
    private String imageUrl;
    private Long likeCount;
    private Long dislikeCount;
    private Long commentCount;
    private Set<String> categories;
    private Constants.PackageStatusEnum status;
    private String rejectionReason;
}
