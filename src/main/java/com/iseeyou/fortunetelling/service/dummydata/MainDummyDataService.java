package com.iseeyou.fortunetelling.service.dummydata;

import com.iseeyou.fortunetelling.service.dummydata.domain.Accounts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class MainDummyDataService implements CommandLineRunner {

    private final Accounts accounts;

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
    }
}
