package com.iseeyou.fortunetelling.service.servicepackage;

import com.iseeyou.fortunetelling.dto.request.servicepackage.ServicePackageUpsertRequest;
import com.iseeyou.fortunetelling.dto.response.ServicePackageDetailResponse;
import com.iseeyou.fortunetelling.dto.response.servicepackage.ServicePackageResponse;
import com.iseeyou.fortunetelling.entity.servicepackage.ServicePackage;
import com.iseeyou.fortunetelling.util.Constants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface ServicePackageService {
    Page<ServicePackage> findAllAvailable(Pageable pageable);
    Page<ServicePackage> findAvailableWithFilters(String name, String categoryIds, Double minPrice, Double maxPrice, Integer minDuration, Integer maxDuration, Pageable pageable);
    Page<ServicePackage> findAvailableByCategoryWithFilters(Constants.ServiceCategoryEnum category, Double minPrice, Double maxPrice, Pageable pageable);
    ServicePackage findById(String id);
    ServicePackage createOrUpdatePackage(ServicePackageUpsertRequest request) throws IOException;
    ServicePackage createOrUpdatePackage(String id, ServicePackageUpsertRequest request) throws IOException;
    String uploadImage(MultipartFile image) throws IOException;
    ServicePackageDetailResponse findDetailById(String id);
    void deleteServicePackage(String id);

    // Interaction methods merged from PackageInteractionService
    ServicePackageResponse toggleInteraction(UUID packageId, Constants.InteractionTypeEnum interactionType);
    ServicePackageResponse getPackageWithInteractions(UUID packageId);
    
    // Method to get all packages with interactions
    Page<ServicePackageResponse> getAllPackagesWithInteractions(Pageable pageable,
                                                                String searchText,
                                                                Double minPrice, Double maxPrice,
                                                                List<UUID> packageCategoryIds,
                                                                List<UUID> seerSpecialityIds,
                                                                Integer minTime, Integer maxTime
    );
    Page<ServicePackageResponse> getPackagesByCategoryWithInteractions(Constants.ServiceCategoryEnum category, Pageable pageable, Double minPrice, Double maxPrice);

    // Seer
    Page<ServicePackageResponse> getMyPackages(Pageable pageable);

    // Admin methods
    ServicePackage confirmServicePackage(String packageId, Constants.PackageStatusEnum status, String rejectionReason);
    Page<ServicePackageResponse> getAllHiddenPackages(Pageable pageable);

    // Review related methods (merged from ServiceReviewService)
    Page<com.iseeyou.fortunetelling.entity.servicepackage.ServiceReview> getTopLevelReviewsByPackage(java.util.UUID packageId, Pageable pageable);
    com.iseeyou.fortunetelling.entity.servicepackage.ServiceReview getReviewById(java.util.UUID id);
    Page<com.iseeyou.fortunetelling.entity.servicepackage.ServiceReview> getReplies(java.util.UUID reviewId, Pageable pageable);
    com.iseeyou.fortunetelling.entity.servicepackage.ServiceReview createReview(java.util.UUID packageId, com.iseeyou.fortunetelling.entity.servicepackage.ServiceReview review);
    com.iseeyou.fortunetelling.entity.servicepackage.ServiceReview updateReview(java.util.UUID id, com.iseeyou.fortunetelling.entity.servicepackage.ServiceReview review);
    void deleteReview(java.util.UUID id);
}
