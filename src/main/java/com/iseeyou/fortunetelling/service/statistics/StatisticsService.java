package com.iseeyou.fortunetelling.service.statistics;

import java.util.Map;

public interface StatisticsService {


    Map<Integer, Long> getNewUsersByMonth(Integer year);


    Map<String, Long> getPackageDistributionByCategory();
}
