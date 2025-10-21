package com.iseeyou.fortunetelling.service.report;

import com.iseeyou.fortunetelling.entity.report.Report;
import com.iseeyou.fortunetelling.entity.report.ReportType;
import com.iseeyou.fortunetelling.util.Constants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;
import java.util.UUID;

public interface ReportService {
    Page<Report> findAllReports(Pageable pageable);
    Page<Report> findAllReportsByReporterId(UUID reporterId, Pageable pageable);
    Page<Report> findAllReportsByReportedUserId(UUID reportedUserId, Pageable pageable);
    Page<Report> findAllReportsByReportedTargetId(UUID reportedTargetId, Pageable pageable);
    Page<Report> findAllReportsByTargetType(Constants.TargetReportTypeEnum targetType, Pageable pageable);
    Page<Report> findAllReportsByStatus(Constants.ReportStatusEnum status, Pageable pageable);
    Page<ReportType> findAllReportTypes(Pageable pageable);
    Report findReportById(UUID id);
    Report createReport(Report report, Constants.ReportTypeEnum reportTypeEnum, Set<String> evidenceUrls);
    Report deleteReport(UUID id);
    Report updateReport(UUID id, Report report);
}
