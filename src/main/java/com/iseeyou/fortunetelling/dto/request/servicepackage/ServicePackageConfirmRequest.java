package com.iseeyou.fortunetelling.dto.request.servicepackage;

import com.iseeyou.fortunetelling.util.Constants;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ServicePackageConfirmRequest {
    @NotNull(message = "Status is required")
    private Constants.PackageStatusEnum status;

    private String rejectionReason;

    /**
     * Custom validation: If status is REJECTED, rejectionReason is required
     */
    public boolean isValid() {
        if (status == Constants.PackageStatusEnum.REJECTED) {
            return rejectionReason != null && !rejectionReason.trim().isEmpty();
        }
        return true;
    }
}

