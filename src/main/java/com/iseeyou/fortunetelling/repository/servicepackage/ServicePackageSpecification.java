package com.iseeyou.fortunetelling.repository.servicepackage;

import com.iseeyou.fortunetelling.entity.servicepackage.PackageCategory;
import com.iseeyou.fortunetelling.entity.servicepackage.ServicePackage;
import com.iseeyou.fortunetelling.util.Constants;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ServicePackageSpecification {

    public static Specification<ServicePackage> withFilters(
            String name,
            String categoryIds,
            Double minPrice,
            Double maxPrice,
            Integer minDuration,
            Integer maxDuration) {

        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();

            // Only show AVAILABLE packages
            predicate = criteriaBuilder.and(predicate,
                criteriaBuilder.equal(root.get("status"), Constants.PackageStatusEnum.AVAILABLE));

            // Search by name (case-insensitive)
            if (StringUtils.hasText(name)) {
                predicate = criteriaBuilder.and(predicate,
                    criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("packageTitle")),
                        "%" + name.toLowerCase() + "%"
                    )
                );
            }

            // Filter by category IDs
            if (StringUtils.hasText(categoryIds)) {
                List<UUID> categoryUUIDs = parseCategories(categoryIds);
                if (!categoryUUIDs.isEmpty()) {
                    Join<ServicePackage, PackageCategory> packageCategoryJoin = root.join("packageCategories");
                    predicate = criteriaBuilder.and(predicate,
                        packageCategoryJoin.get("knowledgeCategory").get("id").in(categoryUUIDs)
                    );
                }
            }

            // Filter by price range
            if (minPrice != null) {
                predicate = criteriaBuilder.and(predicate,
                    criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice)
                );
            }
            if (maxPrice != null) {
                predicate = criteriaBuilder.and(predicate,
                    criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice)
                );
            }

            // Filter by duration range
            if (minDuration != null) {
                predicate = criteriaBuilder.and(predicate,
                    criteriaBuilder.greaterThanOrEqualTo(root.get("durationMinutes"), minDuration)
                );
            }
            if (maxDuration != null) {
                predicate = criteriaBuilder.and(predicate,
                    criteriaBuilder.lessThanOrEqualTo(root.get("durationMinutes"), maxDuration)
                );
            }

            // Make distinct to avoid duplicates when joining with categories
            if (StringUtils.hasText(categoryIds)) {
                query.distinct(true);
            }

            return predicate;
        };
    }

    private static List<UUID> parseCategories(String categoryIds) {
        try {
            return Arrays.stream(categoryIds.split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .map(UUID::fromString)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            // Invalid UUID format, return empty list
            return List.of();
        }
    }

    public static Specification<ServicePackage> availableOnly() {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("status"), Constants.PackageStatusEnum.AVAILABLE);
    }

    public static Specification<ServicePackage> hiddenOnly() {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("status"), Constants.PackageStatusEnum.HIDDEN);
    }
}
