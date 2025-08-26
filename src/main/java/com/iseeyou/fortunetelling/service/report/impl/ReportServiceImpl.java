package com.iseeyou.fortunetelling.service.report.impl;

import com.iseeyou.fortunetelling.entity.report.Report;
import com.iseeyou.fortunetelling.entity.report.ReportEvidence;
import com.iseeyou.fortunetelling.entity.report.ReportType;
import com.iseeyou.fortunetelling.exception.NotFoundException;
import com.iseeyou.fortunetelling.repository.ReportEvidenceRepository;
import com.iseeyou.fortunetelling.repository.ReportRepository;
import com.iseeyou.fortunetelling.repository.ReportTypeRepository;
import com.iseeyou.fortunetelling.service.fileupload.CloudinaryService;
import com.iseeyou.fortunetelling.service.report.ReportService;
import com.iseeyou.fortunetelling.service.user.UserService;
import com.iseeyou.fortunetelling.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final ReportEvidenceRepository reportEvidenceRepository;
    private final ReportTypeRepository reportTypeRepository;
    private final UserService userService;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional(readOnly = true)
    public Page<Report> findAllReports(Pageable pageable) {
        return reportRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Report> findAllReportsByReporterId(UUID reporterId, Pageable pageable) {
        return reportRepository.findAllByReporter_Id(reporterId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Report> findAllReportsByReportedUserId(UUID reportedUserId, Pageable pageable) {
        return reportRepository.findAllByReportedUser_Id(reportedUserId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Report> findAllReportsByReportedTargetId(UUID reportedTargetId, Pageable pageable) {
        return reportRepository.findAllByTargetId(reportedTargetId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Report> findAllReportsByTargetType(Constants.TargetReportTypeEnum targetType, Pageable pageable) {
        return reportRepository.findAllByTargetType(targetType, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Report> findAllReportsByStatus(Constants.ReportStatusEnum status, Pageable pageable) {
        return reportRepository.findAllByStatus(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReportType> findAllReportTypes(Pageable pageable) {
        return reportTypeRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Report findReportById(UUID id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Report not found with id: " + id));
    }

    @Override
    @Transactional
    public Report createReport(Report report, Set<UUID> reportTypeIds, Set<String> evidenceUrls) {
        // Set the reporter to current user
        report.setReporter(userService.getUser());
        report.setStatus(Constants.ReportStatusEnum.PENDING);

        // Handle report type - assuming we take the first one from the set since entity only supports one
        if (reportTypeIds != null && !reportTypeIds.isEmpty()) {
            UUID reportTypeId = reportTypeIds.iterator().next();
            ReportType reportType = reportTypeRepository.findById(reportTypeId)
                    .orElseThrow(() -> new NotFoundException("ReportType not found with id: " + reportTypeId));
            report.setReportType(reportType);
        }

        // Save the report first
        Report savedReport = reportRepository.save(report);

        // Handle evidence URLs
        if (evidenceUrls != null && !evidenceUrls.isEmpty()) {
            for (String evidenceUrl : evidenceUrls) {
                ReportEvidence evidence = ReportEvidence.builder()
                        .report(savedReport)
                        .evidenceImageUrl(evidenceUrl)
                        .build();
                reportEvidenceRepository.save(evidence);
            }
        }

        log.info("Created new report with id: {} for target type: {} with target id: {}",
                savedReport.getId(), report.getTargetType(), report.getTargetId());

        return savedReport;
    }

    @Override
    @Transactional
    public Report deleteReport(UUID id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Report not found with id: " + id));

        log.info("Deleting report with id: {}", id);
        reportRepository.delete(report);

        return report;
    }

    @Override
    @Transactional
    public Report updateReport(UUID id, Report report) {
        Report existingReport = reportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Report not found with id: " + id));

        existingReport.setStatus(report.getStatus());
        if (report.getStatus() == Constants.ReportStatusEnum.RESOLVED) {
            existingReport.setNote(report.getNote());
        }
        existingReport.setActionType(report.getActionType());

        log.info("Updated report with id: {}", id);

        return reportRepository.save(existingReport);
    }
}
