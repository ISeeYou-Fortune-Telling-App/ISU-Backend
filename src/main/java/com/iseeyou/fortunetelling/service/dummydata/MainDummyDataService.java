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
// import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
// @Service  // Comment lại để không tự động chạy DummyData nữa
public class MainDummyDataService implements CommandLineRunner {

    private final Accounts accounts;
    private final Knowledge knowledge;
    private final ServicePackages servicePackages;
    private final Reports reports;
    private final Bookings bookings;
    private final Conversations conversations;

    @Override
    public void run(String... args) {

    }
}
