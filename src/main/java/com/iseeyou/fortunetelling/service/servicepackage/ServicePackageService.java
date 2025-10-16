package com.iseeyou.fortunetelling.service.servicepackage;

import com.iseeyou.fortunetelling.dto.request.servicepackage.ServicePackageUpsertRequest;
import com.iseeyou.fortunetelling.dto.response.ServicePackageDetailResponse;
import com.iseeyou.fortunetelling.entity.servicepackage.ServicePackage;
import com.iseeyou.fortunetelling.util.Constants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ServicePackageService {
    Page<ServicePackage> findAllAvailable(Pageable pageable);
    Page<ServicePackage> findAvailableWithFilters(Double minPrice, Double maxPrice, Pageable pageable);
    Page<ServicePackage> findAvailableByCategoryWithFilters(Constants.ServiceCategoryEnum category, Double minPrice, Double maxPrice, Pageable pageable);
    ServicePackage findById(String id);
    ServicePackage createOrUpdatePackage(String seerId, ServicePackageUpsertRequest request);
    String uploadImage(MultipartFile image);
    ServicePackageDetailResponse findDetailById(String id);
}
