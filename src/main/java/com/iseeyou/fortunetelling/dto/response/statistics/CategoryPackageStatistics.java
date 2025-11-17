package com.iseeyou.fortunetelling.dto.response.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryPackageStatistics {
    private String categoryName;
    private Long packageCount;
}

