package com.iseeyou.fortunetelling.dto.request.certificate;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "Certificate name", example = "Tarot Master Certificate", requiredMode = Schema.RequiredMode.REQUIRED)
    private String certificateName;

    @Schema(description = "Certificate description", example = "Professional certification in Tarot reading")
    private String certificateDescription;

    @NotBlank(message = "Issued by is required")
    @Schema(description = "Organization that issued the certificate", example = "International Tarot Association", requiredMode = Schema.RequiredMode.REQUIRED)
    private String issuedBy;

    @NotNull(message = "Issued at is required")
    @Schema(description = "Date when certificate was issued", example = "2020-01-01T00:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime issuedAt;

    @Schema(description = "Certificate expiration date", example = "2030-01-01T00:00:00")
    private LocalDateTime expirationDate;

    @Schema(description = "Certificate file (image/PDF)", type = "string", format = "binary", requiredMode = Schema.RequiredMode.REQUIRED)
    private MultipartFile certificateFile;

    @Schema(description = "Knowledge category IDs that this certificate relates to", example = "[\"3fa85f64-5717-4562-b3fc-2c963f66afa6\"]")
    private Set<UUID> categoryIds;
}