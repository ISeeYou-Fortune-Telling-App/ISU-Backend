package com.iseeyou.fortunetelling.service.report;

import com.iseeyou.fortunetelling.dto.request.report.ReportCreateRequest;
import com.iseeyou.fortunetelling.dto.request.report.ReportUpdateRequest;
import com.iseeyou.fortunetelling.dto.response.report.ReportStatsResponse;
import com.iseeyou.fortunetelling.entity.report.Report;
import com.iseeyou.fortunetelling.entity.report.ReportType;
import com.iseeyou.fortunetelling.util.Constants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.UUID;

public interface ReportService {
    Page<Report> findAllReports(Pageable pageable, Constants.ReportStatusEnum status, String reportTypeName, Constants.TargetReportTypeEnum targetType);
    Page<Report> findAllReportsByReporterId(UUID reporterId, Pageable pageable);
    Page<Report> findAllReportsByReportedUserId(UUID reportedUserId, Pageable pageable);
    Page<Report> findAllReportsByReportedTargetId(UUID reportedTargetId, Pageable pageable);
    Page<Report> findAllReportsByTargetType(Constants.TargetReportTypeEnum targetType, Pageable pageable);
    Page<Report> findAllReportsByStatus(Constants.ReportStatusEnum status, Pageable pageable);
    Page<ReportType> findAllReportTypes(Pageable pageable);
    Report findReportById(UUID id);
    Report createReport(ReportCreateRequest request) throws IOException;
    Report deleteReport(UUID id);
    Report updateReport(UUID id, ReportUpdateRequest request);
    ReportStatsResponse getStatistics();
}
