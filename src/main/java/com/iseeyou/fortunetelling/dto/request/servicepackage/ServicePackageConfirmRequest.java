package com.iseeyou.fortunetelling.dto.request.servicepackage;

import com.iseeyou.fortunetelling.util.Constants;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ServicePackageConfirmRequest {
    @NotNull(message = "Action is required")
    private Constants.PackageActionEnum action;

    private String rejectionReason;
}
