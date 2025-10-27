package com.iseeyou.fortunetelling.service.dummydata;

import com.iseeyou.fortunetelling.service.dummydata.domain.Accounts;
import com.iseeyou.fortunetelling.service.dummydata.domain.Bookings;
import com.iseeyou.fortunetelling.service.dummydata.domain.Conversations;
import com.iseeyou.fortunetelling.service.dummydata.domain.Knowledge;
import com.iseeyou.fortunetelling.service.dummydata.domain.Reports;
import com.iseeyou.fortunetelling.service.dummydata.domain.ServicePackages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class MainDummyDataService implements CommandLineRunner {

    private final Accounts accounts;
    private final Knowledge knowledge;
    private final ServicePackages servicePackages;
    private final Reports reports;
    private final Bookings bookings;
    private final Conversations conversations;

    @Override
    public void run(String... args) {
        log.info("Generating dummy data...");
        try {
            accounts.createDummyData();
        } catch (Exception e) {
            log.info("Dummy data already exists, skipping creation.");
            return;
        }
        log.info("Dummy data generation completed.");

        log.info("Creating knowledge categories...");
        knowledge.createDummyData();
        log.info("Knowledge categories created successfully.");

        log.info("Creating service packages...");
        servicePackages.createDummyData();
        log.info("Service packages created successfully.");

        log.info("Creating reports, report types and evidences...");
        reports.createDummyData();
        log.info("Reports created successfully.");

        log.info("Creating bookings, payments and reviews...");
        bookings.createDummyData();
        log.info("Bookings created successfully.");

        log.info("Creating conversations and messages...");
        conversations.createDummyData();
        log.info("Conversations created successfully.");
    }
}
