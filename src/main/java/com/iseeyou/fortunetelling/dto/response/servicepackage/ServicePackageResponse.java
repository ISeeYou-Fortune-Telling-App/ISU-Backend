package com.iseeyou.fortunetelling.dto.response.servicepackage;

import com.iseeyou.fortunetelling.entity.ServicePackage;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@SuperBuilder
public class ServicePackageResponse {
    private String id;
    private String seerId;
    private String packageTitle;
    private String packageContent;
    private String imageUrl;
    private Integer durationMinutes;
    private Double price;
    private ServicePackage.ServicePackageStatus status;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
