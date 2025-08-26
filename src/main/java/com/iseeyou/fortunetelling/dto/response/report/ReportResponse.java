package com.iseeyou.fortunetelling.dto.response.report;

import com.iseeyou.fortunetelling.dto.response.AbstractBaseDataResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ReportResponse extends AbstractBaseDataResponse {
    private UUID reporterId;
    private UserInfo reporter;
    private UserInfo reported;
    private String targetReportType;
    private UUID targetId;
    private String reportType;
    private String reportDescription;
    private String reportStatus;
    private String actionType;
    private String note;

    @Getter
    @Setter
    public static class UserInfo {
        private UUID id;
        private String username;
        private String avatarUrl;
    }
}
