package com.iseeyou.fortunetelling.service.statistics.impl;

import com.iseeyou.fortunetelling.dto.response.statistics.CategoryPackageStatistics;
import com.iseeyou.fortunetelling.dto.response.statistics.MonthlyUserStatistics;
import com.iseeyou.fortunetelling.repository.servicepackage.PackageCategoryRepository;
import com.iseeyou.fortunetelling.repository.user.UserRepository;
import com.iseeyou.fortunetelling.service.statistics.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final UserRepository userRepository;
    private final PackageCategoryRepository packageCategoryRepository;

    @Override
    @Transactional(readOnly = true)
    public Map<Integer, Long> getNewUsersByMonth(Integer year) {
        List<MonthlyUserStatistics> statistics = userRepository.getNewUsersByMonth(year);

        // Tạo map với tất cả 12 tháng, mặc định count = 0
        Map<Integer, Long> result = new LinkedHashMap<>();
        for (int month = 1; month <= 12; month++) {
            result.put(month, 0L);
        }

        // Cập nhật count cho các tháng có dữ liệu
        statistics.forEach(stat -> result.put(stat.getMonth(), stat.getCount()));

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getPackageDistributionByCategory() {
        List<CategoryPackageStatistics> statistics = packageCategoryRepository.getPackageDistributionByCategory();

        return statistics.stream()
                .collect(Collectors.toMap(
                        CategoryPackageStatistics::getCategoryName,
                        CategoryPackageStatistics::getPackageCount,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }
}

