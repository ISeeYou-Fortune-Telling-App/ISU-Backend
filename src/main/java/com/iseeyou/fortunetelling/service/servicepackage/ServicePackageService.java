package com.iseeyou.fortunetelling.service.servicepackage;

import com.iseeyou.fortunetelling.dto.request.servicepackage.ServicePackageUpsertRequest;
import com.iseeyou.fortunetelling.dto.response.ServicePackageDetailResponse;
import com.iseeyou.fortunetelling.dto.response.servicepackage.ServicePackageResponse;
import com.iseeyou.fortunetelling.entity.servicepackage.ServicePackage;
import com.iseeyou.fortunetelling.util.Constants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ServicePackageService {
    Page<ServicePackage> findAllAvailable(Pageable pageable);
    Page<ServicePackage> findAvailableWithFilters(Double minPrice, Double maxPrice, Pageable pageable);
    Page<ServicePackage> findAvailableByCategoryWithFilters(Constants.ServiceCategoryEnum category, Double minPrice, Double maxPrice, Pageable pageable);
    ServicePackage findById(String id);
    ServicePackage createOrUpdatePackage(ServicePackageUpsertRequest request);
    ServicePackage createOrUpdatePackage(String id, ServicePackageUpsertRequest request);
    String uploadImage(MultipartFile image);
    ServicePackageDetailResponse findDetailById(String id);
    void deleteServicePackage(String id);
    
    // Interaction methods merged from PackageInteractionService
    ServicePackageResponse toggleInteraction(UUID packageId, Constants.InteractionTypeEnum interactionType);
    ServicePackageResponse getPackageWithInteractions(UUID packageId);
    
    // Method to get all packages with interactions - Enhanced with search and filters
    Page<ServicePackageResponse> getAllPackagesWithInteractions(
            Pageable pageable,
            String searchKeyword,
            List<String> categoryIds,
            Double minPrice,
            Double maxPrice,
            Integer minDuration,
            Integer maxDuration
    );
    Page<ServicePackageResponse> getPackagesByCategoryWithInteractions(Constants.ServiceCategoryEnum category, Pageable pageable, Double minPrice, Double maxPrice);

    // Admin methods
    ServicePackage confirmServicePackage(String packageId, Constants.PackageStatusEnum status, String rejectionReason);
    Page<ServicePackageResponse> getAllHiddenPackages(Pageable pageable);
}
