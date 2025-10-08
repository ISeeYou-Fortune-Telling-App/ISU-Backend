package com.iseeyou.fortunetelling.service.servicepackage;

import com.iseeyou.fortunetelling.entity.ServicePackage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ServicePackageService {
    Page<ServicePackage> findAllAvailable(Pageable pageable);
    Page<ServicePackage> findAvailableWithFilters(Double minPrice, Double maxPrice, Pageable pageable);
    ServicePackage findById(String id);
}
