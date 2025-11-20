package com.iseeyou.fortunetelling.dto.request.report;

import com.iseeyou.fortunetelling.util.Constants;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportViolationActionRequest {
    @NotNull(message = "Action is required")
    private ViolationAction action;

    @NotNull(message = "Decision reason is required")
    private String decisionReason;

    // Only for SUSPEND action
    private Integer suspensionDays;

    public enum ViolationAction {
        WARNING,
        SUSPEND,
        BAN
    }
}

