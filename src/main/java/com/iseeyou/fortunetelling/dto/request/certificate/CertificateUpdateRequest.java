package com.iseeyou.fortunetelling.dto.request.certificate;

import com.iseeyou.fortunetelling.util.Constants;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class CertificateUpdateRequest {
    private UUID certificateId;
    private String certificateName;
    private String certificateDescription;
    private String issuedBy;
    private LocalDateTime issuedAt;
    private LocalDateTime expirationDate;
    private MultipartFile certificateFile;
    private Constants.CertificateStatusEnum status;
    private String decisionReason;
    private LocalDateTime decisionDate;
    private Set<UUID> categoryIds;
}