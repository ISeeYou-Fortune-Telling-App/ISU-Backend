package com.iseeyou.fortunetelling.repository.servicepackage;

import com.iseeyou.fortunetelling.entity.servicepackage.PackageInteraction;
import com.iseeyou.fortunetelling.util.Constants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PackageInteractionRepository extends JpaRepository<PackageInteraction, UUID>, JpaSpecificationExecutor<PackageInteraction> {

    Optional<PackageInteraction> findByUser_IdAndServicePackage_Id(UUID userId, UUID packageId);

    boolean existsByUser_IdAndServicePackage_Id(UUID userId, UUID packageId);

    long countByServicePackage_IdAndInteractionType(UUID packageId, Constants.InteractionTypeEnum interactionType);
    
    void deleteByUser_IdAndServicePackage_Id(UUID userId, UUID packageId);

    @Query("SELECT pi FROM PackageInteraction pi JOIN FETCH pi.user WHERE pi.servicePackage.id = :packageId")
    List<PackageInteraction> findAllByServicePackage_IdWithUser(@Param("packageId") UUID packageId);
}
