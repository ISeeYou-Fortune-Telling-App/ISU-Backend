package com.iseeyou.fortunetelling.dto.response.certificate;

import com.iseeyou.fortunetelling.dto.response.BaseDataResponse;
import com.iseeyou.fortunetelling.util.Constants;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class CertificateResponse extends BaseDataResponse {
    private String seerName;
    private String certificateName;
    private String certificateDescription;
    private String issuedBy;
    private LocalDateTime issuedAt;
    private LocalDateTime expirationDate;
    private String certificateUrl;
    private Constants.CertificateStatusEnum status;
    private String decisionReason;
    private LocalDateTime decisionDate;
    private Set<String> categories;
}
