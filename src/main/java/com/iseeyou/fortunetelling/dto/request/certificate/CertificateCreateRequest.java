package com.iseeyou.fortunetelling.dto.request.certificate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CertificateCreateRequest {
    @NotBlank(message = "Certificate name is required")
    private String certificateName;

    private String certificateDescription;

    @NotBlank(message = "Issued by is required")
    private String issuedBy;

    @NotNull(message = "Issued at is required")
    private LocalDateTime issuedAt;

    private LocalDateTime expirationDate;

    private MultipartFile certificateFile;

    private Set<UUID> categoryIds;
}