package com.iseeyou.fortunetelling.dto.response.servicepackage;

import com.iseeyou.fortunetelling.dto.response.PageResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class ServicePackagePageResponse extends PageResponse<ServicePackageResponse> {
    private ServicePackageStats stats;

    @Data
    @Builder
    @AllArgsConstructor
    public static class ServicePackageStats {
        private Long totalPackages;
        private Long reportedPackages;
        private Long hiddenPackages;
        private Long totalInteractions;
    }

    public ServicePackagePageResponse(int statusCode, String message, List<ServicePackageResponse> data,
                                     PageResponse.PagingResponse paging, ServicePackageStats stats) {
        super(statusCode, message, data, paging);
        this.stats = stats;
    }
}

