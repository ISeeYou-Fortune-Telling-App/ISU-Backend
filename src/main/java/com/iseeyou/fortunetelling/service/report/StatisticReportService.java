package com.iseeyou.fortunetelling.service.report;

import com.iseeyou.fortunetelling.dto.response.report.SeerSimpleRating;

public interface StatisticReportService {
    SeerSimpleRating getSeerSimpleRating(String seerId, Integer month, Integer year);
}

