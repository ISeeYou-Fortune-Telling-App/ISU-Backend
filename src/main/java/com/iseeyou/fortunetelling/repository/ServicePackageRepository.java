package com.iseeyou.fortunetelling.repository;

import com.iseeyou.fortunetelling.entity.ServicePackage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServicePackageRepository extends JpaRepository<ServicePackage, UUID>, JpaSpecificationExecutor<ServicePackage> {

    Page<ServicePackage> findByStatus(ServicePackage.ServicePackageStatus status, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT sp FROM ServicePackage sp WHERE sp.status = 'AVAILABLE' AND " +
            "(:minPrice IS NULL OR sp.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR sp.price <= :maxPrice)")
    Page<ServicePackage> findAvailablePackagesWithPriceFilter(@Param("minPrice") Double minPrice,
                                                              @Param("maxPrice") Double maxPrice,
                                                              Pageable pageable);

    Optional<ServicePackage> findById(UUID packageId);
}
