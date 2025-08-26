package com.iseeyou.fortunetelling.repository.servicepackage;

import com.iseeyou.fortunetelling.entity.servicepackage.PackageInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PackageInteractionRepository extends JpaRepository<PackageInteraction, UUID>, JpaSpecificationExecutor<PackageInteraction> {

    Optional<PackageInteraction> findByUser_IdAndServicePackage_Id(UUID userId, UUID packageId);

    boolean existsByUser_IdAndServicePackage_Id(UUID userId, UUID packageId);

    long countByServicePackage_IdAndIsLikeTrue(UUID packageId);

    long countByServicePackage_IdAndIsLikeFalse(UUID packageId);
}
