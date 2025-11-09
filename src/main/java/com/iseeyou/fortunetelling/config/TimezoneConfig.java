package com.iseeyou.fortunetelling.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

@Configuration
@Slf4j
public class TimezoneConfig {

    @Value("${app.default-timezone:Asia/Ho_Chi_Minh}")
    private String defaultTimezone;

    @PostConstruct
    public void init() {
        TimeZone timeZone = TimeZone.getTimeZone(defaultTimezone);
        TimeZone.setDefault(timeZone);
        log.info("✅ Application timezone set to: {} (UTC offset: {})",
                 defaultTimezone,
                 timeZone.getDisplayName());
        log.info("Current system time: {}", java.time.LocalDateTime.now());
    }
}

