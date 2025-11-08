package com.iseeyou.fortunetelling.dto.request.certificate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iseeyou.fortunetelling.util.Constants;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateApprovalRequest {
    @JsonProperty("action")
    @NotNull(message = "Action is required (APPROVED or REJECTED)")
    private Constants.CertificateStatusEnum action;

    @JsonProperty("decision_reason")
    private String decisionReason;
}
