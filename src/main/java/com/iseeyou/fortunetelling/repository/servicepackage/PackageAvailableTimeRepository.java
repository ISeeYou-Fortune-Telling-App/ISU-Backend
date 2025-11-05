package com.iseeyou.fortunetelling.repository.servicepackage;

import com.iseeyou.fortunetelling.entity.servicepackage.PackageAvailableTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PackageAvailableTimeRepository extends JpaRepository<PackageAvailableTime, UUID> {

    List<PackageAvailableTime> findByServicePackageId(UUID packageId);

    @Modifying
    @Query("DELETE FROM PackageAvailableTime pat WHERE pat.servicePackage.id = :packageId")
    void deleteByServicePackageId(@Param("packageId") UUID packageId);

    List<PackageAvailableTime> findByServicePackageIdAndWeekDate(UUID packageId, Integer weekDate);
}

