package com.iseeyou.fortunetelling.service.report;

import com.iseeyou.fortunetelling.dto.request.report.ReportViolationActionRequest;
import com.iseeyou.fortunetelling.entity.report.Report;

import java.util.UUID;

public interface ReportViolationService {
    Report handleViolationAction(UUID reportId, ReportViolationActionRequest request);
    void checkAndReactivateSuspendedAccounts();
}

