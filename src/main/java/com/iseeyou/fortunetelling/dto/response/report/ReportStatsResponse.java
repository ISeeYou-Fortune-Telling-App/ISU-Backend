package com.iseeyou.fortunetelling.dto.response.report;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportStatsResponse {
    private Long totalReports;
    private Long newReportsThisMonth;
    private Long resolvedReports;
    private Long unresolvedReports;
}

