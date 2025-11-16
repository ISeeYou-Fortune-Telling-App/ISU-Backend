package com.iseeyou.fortunetelling.dto.response.certificate;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateStatsResponse {
    private Long totalCertificates;
    private Long approvedCertificates;
    private Long pendingCertificates;
    private Long rejectedCertificates;
}

