package com.iseeyou.fortunetelling.service.report.impl;

import com.iseeyou.fortunetelling.dto.response.report.SeerSimpleRating;
import com.iseeyou.fortunetelling.service.report.ReportService;
import com.iseeyou.fortunetelling.service.report.StatisticReportService;
import com.iseeyou.fortunetelling.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticReportImpl implements StatisticReportService {

    private final RestTemplate restTemplate;

    @Value("${report.service.uri:http://localhost:8086}")
    private String reportServiceUri;

    @Override
    public SeerSimpleRating getSeerSimpleRating(String seerId, Integer month, Integer year) {
        try {
            log.info("Calling Report Service to get seer simple rating: seerId={}, month={}, year={}", seerId, month, year);
            log.info("Report Service URI: {}", reportServiceUri);

            // Build URL with query parameters
            String url = String.format("%s/internal/statistic-report/seer-simple-rating?seerId=%s&month=%d&year=%d",
                    reportServiceUri, seerId, month, year);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            log.info("Calling Report Service at: {}", url);

            ResponseEntity<SeerSimpleRating> response;
            try {
                response = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        entity,
                        SeerSimpleRating.class
                );
                log.info("Report Service response status: {}", response.getStatusCode());
                log.info("Report Service response body: {}", response.getBody());
            } catch (Exception e) {
                log.error("Report Service call failed with error: {}", e.getMessage(), e);

                // Check if it's a connection issue
                if (e.getMessage().contains("Connection refused") || e.getMessage().contains("ConnectException")) {
                    log.warn("Cannot connect to Report Service at: {}", reportServiceUri);
                } else if (e.getMessage().contains("timeout") || e.getMessage().contains("TimeoutException")) {
                    log.warn("Report Service request timed out");
                } else {
                    log.warn("Unknown error when calling Report Service: {}", e.getClass().getSimpleName());
                }

                // Return fallback response when Report Service fails
                return SeerSimpleRating.builder()
                        .totalRates(0)
                        .avgRating(BigDecimal.ZERO)
                        .performanceTier("BRONZE")
                        .build();
            }

            if (response.getBody() == null) {
                log.error("Report Service returned null response body");
                // Return default values
                return SeerSimpleRating.builder()
                        .totalRates(0)
                        .avgRating(BigDecimal.ZERO)
                        .performanceTier("BRONZE")
                        .build();
            }

            return response.getBody();

        } catch (Exception e) {
            log.error("Error getting seer simple rating from Report Service", e);
            return SeerSimpleRating.builder()
                    .totalRates(0)
                    .avgRating(BigDecimal.ZERO)
                    .performanceTier("BRONZE")
                    .build();
        }
    }
}
