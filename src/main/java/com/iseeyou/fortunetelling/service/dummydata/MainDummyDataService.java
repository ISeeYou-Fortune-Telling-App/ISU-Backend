package com.iseeyou.fortunetelling.service.dummydata;

import com.iseeyou.fortunetelling.service.dummydata.domain.Accounts;
import com.iseeyou.fortunetelling.service.dummydata.domain.Knowledge;
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
    }
}
