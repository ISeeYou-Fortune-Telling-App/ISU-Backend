package com.iseeyou.fortunetelling.dto.request.report;

import com.iseeyou.fortunetelling.util.Constants;
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
    private UUID reporterId;
    private UUID reportedId;
    private Constants.TargetReportTypeEnum targetReportType;
    private UUID targetId;
    private UUID reportTypeId;
    private String description;
    private MultipartFile[] imageFiles;
}
