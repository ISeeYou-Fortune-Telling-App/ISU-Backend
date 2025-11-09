package com.iseeyou.fortunetelling.dto.request.user;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ApproveSeerRequest {
    @NotNull(message = "Action is required")
    private SeerApprovalAction action;

    private String rejectReason;

    public enum SeerApprovalAction {
        APPROVED,
        REJECTED
    }
}

