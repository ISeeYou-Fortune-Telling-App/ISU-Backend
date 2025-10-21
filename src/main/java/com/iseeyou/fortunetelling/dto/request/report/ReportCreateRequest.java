package com.iseeyou.fortunetelling.dto.request.report;

import com.iseeyou.fortunetelling.util.Constants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ReportCreateRequest {
    @NotNull(message = "Target report type is required")
    @Schema(description = "Target report type (SEER, SERVICE_PACKAGE, BOOKING, CHAT)", required = true, example = "SEER")
    private Constants.TargetReportTypeEnum targetReportType;

    @NotNull(message = "Target ID is required")
    @Schema(description = "Target ID (ID of the seer/package/booking/conversation being reported)", required = true, example = "123e4567-e89b-12d3-a456-426614174002")
    private UUID targetId;

    @NotNull(message = "Report type is required")
    @Schema(description = "Report type (SPAM, INAPPROPRIATE_CONTENT, HARASSMENT, HATE_SPEECH, VIOLENCE, NUDITY, COPYRIGHT, IMPERSONATION, FRAUD, OTHER)", 
            required = true, example = "SPAM")
    private Constants.ReportTypeEnum reportType;

    @Schema(description = "Description of the report", example = "This user is posting inappropriate content")
    private String description;

    private MultipartFile[] imageFiles;
}
